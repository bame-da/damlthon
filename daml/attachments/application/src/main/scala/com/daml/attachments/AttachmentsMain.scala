// Copyright (c) 2019 The DAML Authors. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package com.daml.attachments

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.daml.attachments.model.{Attachments => M}
import com.digitalasset.api.util.TimeProvider
import com.digitalasset.grpc.adapter.AkkaExecutionSequencerPool
import com.digitalasset.ledger.api.refinements.ApiTypes.{ApplicationId, Party, WorkflowId}
import com.digitalasset.ledger.api.v1.ledger_offset.LedgerOffset
import com.digitalasset.ledger.api.v1.ledger_offset.LedgerOffset.LedgerBoundary
import com.digitalasset.ledger.client.LedgerClient
import com.digitalasset.ledger.client.binding.{Contract, Primitive => P}
import com.digitalasset.ledger.client.configuration.{CommandClientConfiguration, LedgerClientConfiguration, LedgerIdRequirement}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}
import scala.util.control.NonFatal

object AttachmentsMain extends App with StrictLogging {
  if (args.length != 4) {
    logger.error("Usage: LEDGER_HOST LEDGER_PORT SERVER_PORT PARTY")
    logger.error("Setting environment variable EXTERNAL_IP will use it to construct urls")
    logger.error("returned by /upload.")
    System.exit(-1)
  }

  private val ledgerHost = args(0)
  private val ledgerPort = args(1).toInt
  private val serverPort = args(2).toInt
  private val party = P.Party(args(3))

  private val asys = ActorSystem()
  private val amat =
    ActorMaterializer(
      ActorMaterializerSettings(asys)
        .withSupervisionStrategy { e =>
          logger.error(s"Supervision caught exception: $e")
          Supervision.Stop
        }
    )(asys)

  private val aesf = new AkkaExecutionSequencerPool("clientPool")(asys)

  private def shutdown(): Unit = {
    logger.info("Shutting down...")
    Await.result(asys.terminate(), 10.seconds)
    ()
  }

  private implicit val ec: ExecutionContext = asys.dispatcher

  private val applicationId = ApplicationId("AttachmentsBot")

  private val timeProvider = TimeProvider.Constant(Instant.EPOCH)

  private val clientConfig = LedgerClientConfiguration(
    applicationId = ApplicationId.unwrap(applicationId),
    ledgerIdRequirement = LedgerIdRequirement("", enabled = false),
    commandClient = CommandClientConfiguration.default,
    sslContext = None
  )

  private val clientF: Future[LedgerClient] =
    LedgerClient.singleHost(ledgerHost, ledgerPort, clientConfig)(ec, aesf)

  private val clientUtilF: Future[ClientUtil] =
    clientF.map(client => new ClientUtil(client, applicationId, 30.seconds, timeProvider))

  private val acknowledgeWorkflowId: WorkflowId = ClientUtil.workflowIdFromParty(party)


  private def download(expectedHash: String, locations: Seq[(Party, M.URL)]): Option[Array[Byte]] =
    if (locations.isEmpty) {
      None
    } else {
      val loc = locations.head
      logger.info(s"downloading $loc...")
      AttachmentsClient.downloadAttachment(loc._2.unpack) match {
        case Left(err) =>
          logger.warn(s"download: Failed to download from $loc: $err")
          download(expectedHash, locations.tail)
        case Right(data) =>
          if (AttachmentStore.attachmentHash(data) == expectedHash) {
            Some(data)
          } else {
            logger.warn(s"Corrupted data for $expectedHash downloaded from party ${loc._1} with url ${loc._2.unpack}")
            download(expectedHash, locations.tail)
          }
      }
    }

  val acknowledgeFlow: Future[Unit] = for {
    clientUtil <- clientUtilF

    _ <- ActiveProposals(party, LedgerOffset().withBoundary(LedgerBoundary.LEDGER_BEGIN), clientUtil, amat)
      .proposals
      .runForeach { contract: Contract[M.AttachmentProposal] =>
        val proposal = contract.value
        if (proposal.receiver != party) {
          logger.info(s"ignoring contract since we're not the receiver")
        } else {
          logger.info("we're the proposal receiver. fetching the attachment.")

          download(proposal.hash.unpack, proposal.locations.map(t => t._1 -> t._2)) match {
            case None =>
              // FIXME(JM): We need to retry again at a later time!
              sys.error(s"Failed to fetch data for proposal: $proposal!")
            case Some(data) =>
              logger.info("Successfully fetched data, acknowledging...")
              AttachmentStore.insert(data)

              val exerciseCmd =
                contract.contractId.exerciseAcknowledge(
                  actor = party,
                  newLocation = M.URL(s"http://localhost:$serverPort/attachments/${proposal.hash.unpack}")
                )

              clientUtil.submitCommand(
                party,
                acknowledgeWorkflowId,
                exerciseCmd
              ) onComplete {
                case Success(_) =>
                  logger.info("Successfully acknowledged!")

                case Failure(e) =>
                  // FIXME(JM): We should push this back to ActiveProposals for later retry!
                  logger.error(s"Failed to acknowledge: $e")
              }
          }
        }
      }(amat)
  }  yield {
    logger.info("acknowledgeFlow ended!")
    ()
  }

  // Run a sanity check against the server.
  val sanityCheckThread = new Thread {
    override def run(): Unit = {
      logger.info("Running internal sanity check...")
      var retries = 3
      var ok = false
      while (!ok && retries > 0) {
        try {
          AttachmentsClient.roundtripTest("localhost", serverPort, Array(1, 2, 3, 4))
          logger.info("Sanity check OK!")
          ok = true
        } catch {
          case NonFatal(e) =>
            logger.error(s"roundtripTest error: $e, retrying in 3 seconds...")
            Thread.sleep(3000)
            retries -= 1
        }
      }
      if (!ok) {
        logger.error("roundtrip test failed, exiting.")
        System.exit(1)
      }
    }
  }
  sanityCheckThread.start()

  val serverThread = new Thread {
    override def run(): Unit =
      JettyLauncher.launch(serverPort)
  }
  serverThread.start()

  acknowledgeFlow.foreach(_ => shutdown())
}
