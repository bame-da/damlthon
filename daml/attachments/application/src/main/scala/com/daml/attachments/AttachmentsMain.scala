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
  if (System.getenv("LEDGER_HOST") == null) {
    logger.error("Please set following environment variables:")
    logger.error("LEDGER_HOST - Ledger server hostname")
    logger.error("LEDGER_PORT - Ledger server port")
    logger.error("SERVER_PORT - File server port")
    logger.error("PARTY       - DAML party")
    System.exit(-1)
  }

  private val applicationId = ApplicationId("AttachmentsBot")
  private val setup = Setup(applicationId)

  private val aesf = new AkkaExecutionSequencerPool("botClientPool")(setup.asys)

  private def shutdown(): Unit = {
    logger.info("Shutting down...")
    Await.result(setup.asys.terminate(), 10.seconds)
    ()
  }

  private val acknowledgeWorkflowId: WorkflowId = ClientUtil.workflowIdFromParty(setup.party)


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

  private val fileMirrorWorkflowId: WorkflowId = ClientUtil.workflowIdFromParty(setup.party)

  implicit val ec: ExecutionContext = setup.ec

  val fileMirrorFlow: Future[Unit] = for {
    clientUtil <- setup.clientUtilF
    _ <- UnmirroredFiles(setup.party, LedgerOffset().withBoundary(LedgerBoundary.LEDGER_BEGIN), clientUtil)(setup.mat)
        .files
        .runForeach { contract: Contract[model.File.File] =>
          val file = contract.value
          val hash = file.hash.unpack
          if (file.owner == setup.party) {
            logger.info(s"ignoring since we're the owner")
          } else {
            logger.info(s"New file $hash, trying to mirror it from party ${file.owner}...")
            AttachmentsClient.downloadAttachment(file.uri.unpack) match {
              case Left(err) =>
                // FIXME(JM): We need to retry again at a later time!
                logger.error(s"Failed to fetch data when trying to mirror ${file.hash.unpack}")
              case Right(data) =>
                if (AttachmentStore.attachmentHash(data) != hash) {
                  logger.warn(s"File $hash hosted by ${file.owner} is corrupted, refusing to mirror!")
                } else {
                  logger.info("Successfully fetched data, creating the File...")
                  AttachmentStore.insert(data)

                  val observers = contract.value.observers.toSet
                  val createCmd =
                    contract.value.copy(
                      owner = setup.party,
                      observers = (observers - setup.party + contract.value.owner).toList,
                      uri = model.File.URI(AttachmentsServlet.attachmentUrl(setup.serverPort, hash))
                    ).create

                  clientUtil.submitCommand(
                    setup.party,
                    fileMirrorWorkflowId,
                    createCmd
                  ) onComplete {
                    case Success(_) =>
                      logger.info(s"$hash successfully mirrored!")

                    case Failure(e) =>
                      // FIXME(JM): We should retry creating this.
                      logger.error(s"Failed to acknowledge: $e")
                  }
                }
            }
          }
        }(setup.mat)
  }  yield ()

  val acknowledgeFlow: Future[Unit] = for {
    clientUtil <- setup.clientUtilF

    _ <- ActiveProposals(setup.party, LedgerOffset().withBoundary(LedgerBoundary.LEDGER_BEGIN), clientUtil)(setup.mat)
      .proposals
      .runForeach { contract: Contract[M.AttachmentProposal] =>
        val proposal = contract.value
        if (proposal.receiver != setup.party) {
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
                  actor = setup.party,
                  newLocation = M.URL(s"http://localhost:${setup.serverPort}/attachments/${proposal.hash.unpack}")
                )

              clientUtil.submitCommand(
                setup.party,
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
      }(setup.mat)
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
          AttachmentsClient.roundtripTest("localhost", setup.serverPort, Array(1, 2, 3, 4))
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
      JettyLauncher.launch(setup.serverPort)
  }
  serverThread.start()

  Future.firstCompletedOf(
    List(
      acknowledgeFlow,
      fileMirrorFlow
    )
  ).foreach(_ => shutdown())
}
