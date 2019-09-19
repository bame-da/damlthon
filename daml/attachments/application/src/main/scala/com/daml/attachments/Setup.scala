package com.daml.attachments

import java.time.Instant

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.digitalasset.api.util.TimeProvider
import com.digitalasset.grpc.adapter.AkkaExecutionSequencerPool
import com.digitalasset.ledger.api.refinements.ApiTypes.ApplicationId
import com.digitalasset.ledger.client.LedgerClient
import com.digitalasset.ledger.client.binding.{Primitive => P}
import com.digitalasset.ledger.client.configuration.{CommandClientConfiguration, LedgerClientConfiguration, LedgerIdRequirement}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

/** Common stuff to set up the ledger client.
 * Used by the bots and by the servlet. */
case class Setup(applicationId: ApplicationId) {
  val ledgerHost = System.getenv("LEDGER_HOST")
  val ledgerPort = System.getenv("LEDGER_PORT").toInt
  val serverPort = System.getenv("SERVER_PORT").toInt
  val party = P.Party(System.getenv("PARTY"))

  val asys = ActorSystem()
  val mat = ActorMaterializer(
      ActorMaterializerSettings(asys)
        .withSupervisionStrategy { e =>
          sys.error(s"Supervision caught exception: $e")
          Supervision.Stop
        }
    )(asys)

  val aesf = new AkkaExecutionSequencerPool("clientPool")(asys)
  val ec: ExecutionContext = asys.dispatcher

  val timeProvider = TimeProvider.Constant(Instant.EPOCH)
  val clientConfig = LedgerClientConfiguration(
    applicationId = ApplicationId.unwrap(applicationId),
    ledgerIdRequirement = LedgerIdRequirement("", enabled = false),
    commandClient = CommandClientConfiguration.default,
    sslContext = None
  )

  val clientF: Future[LedgerClient] =
    LedgerClient.singleHost(ledgerHost, ledgerPort, clientConfig)(ec, aesf)

  val clientUtilF: Future[ClientUtil] =
    clientF.map(client => new ClientUtil(client, applicationId, 30.seconds, timeProvider))(ec)

}
