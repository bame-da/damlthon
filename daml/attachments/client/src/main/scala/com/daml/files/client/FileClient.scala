package com.daml.files.client

// import com.daml.attachments.AttachmentsMain.{args, logger}
import java.nio.file.{Files, Paths}
import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.daml.attachments.model.{File => M}
import com.daml.attachments.{AttachmentsClient, ClientUtil}
import com.digitalasset.api.util.TimeProvider
import com.digitalasset.grpc.adapter.AkkaExecutionSequencerPool
import com.digitalasset.ledger.api.refinements.ApiTypes.{ApplicationId, WorkflowId}
import com.digitalasset.ledger.client.LedgerClient
import com.digitalasset.ledger.client.binding.{Primitive => P}
import com.digitalasset.ledger.client.configuration.{CommandClientConfiguration, LedgerClientConfiguration, LedgerIdRequirement}
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success}

object FileClient extends App with StrictLogging {
  if (args.length != 7) {
    logger.error("Usage: <ledger host> <ledger port> <server host> <server port> <party> <observers> <filename>")
    logger.error("Observers are comma-delimited, e.g. \"Alice, Bob\"")
    System.exit(-1)
  }

  private val ledgerHost = args(0)
  private val ledgerPort = args(1).toInt
  private val serverHost = args(2)
  private val serverPort = args(3).toInt
  private val party = P.Party(args(4))
  private val observers = args(5).split(',').map(p => P.Party(p.trim))
  private val inputFile = Paths.get(args(6))

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

  private val applicationId = ApplicationId("FileClient")

  private implicit val ec: ExecutionContext = asys.dispatcher

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

  val uploadFileF: Future[Unit] = for {
    clientUtil <- clientUtilF
    bytes = Files.readAllBytes(inputFile)
    resp = AttachmentsClient.uploadAttachment(serverHost, serverPort, bytes)
    workflowId = WorkflowId(s"FileUpload${resp.hash}")

    fileContract = M.File(
      fileName = inputFile.getFileName.toString,
      hash = M.SHA256(resp.hash),
      encryption = M.Encryption.EncNone(()),
      mimeType = M.MimeType("application/octet-stream"),
//        URLConnection.guessContentTypeFromName(inputFile.getFileName.toString)),
      owner = party,
      observers = observers.toList,
      uri = M.URI(resp.url)
    )

    _ = logger.info(s"Creating: $fileContract")
    compl <- clientUtil.submitCommandAndTrack(
      party,
      workflowId,
      fileContract.create
    )(amat)
  } yield {
    logger.info(s"File successfully uploaded! completion: $compl")
  }

  uploadFileF onComplete {
    case Success(_) =>
      logger.info(s"Upload success")

    case Failure(e) =>
      logger.error(s"Failed to create 'File'")
  }
  Await.ready(uploadFileF, 60.seconds)
  shutdown()
}
