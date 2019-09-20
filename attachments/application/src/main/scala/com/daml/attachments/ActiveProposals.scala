package com.daml.attachments

//
// ActiveProposals implements a stream of AttachmentProposals.
// The sources to this stream are:
// - The initial active contract set
// - Past failed acknowledgement attempts which are retried (TODO)
// - New transactions creating AttachmentProposals

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Concat, Source}
import com.daml.attachments.model.{Attachments => M}
import com.digitalasset.ledger.api.refinements.ApiTypes.Party
import com.digitalasset.ledger.api.v1.ledger_offset.LedgerOffset
import com.digitalasset.ledger.client.binding.Contract

import scala.concurrent.Future

case class ActiveProposals(party: Party, offset0: LedgerOffset, clientUtil: ClientUtil)(implicit val amat: Materializer) {
  //private val logger = LoggerFactory.getLogger(this.getClass)

  // A stream of proposals from the snapshot.
  private def snapshot: Future[(LedgerOffset, Stream[Contract[M.AttachmentProposal]])] =
      clientUtil.acsSnapshot(party).runFold(offset0 -> Stream.empty[Contract[M.AttachmentProposal]]) { case ((_, props), acsResponse) =>
        LedgerOffset().withAbsolute(acsResponse.offset) ->
          (props ++ acsResponse.activeContracts
            .flatMap { ev => DecodeUtil.decodeCreated[M.AttachmentProposal](ev).toSeq }
            .filter(contract => contract.value.receiver == party))
      }

  // Stream of new proposals after the active contract snapshot
  private def newProposals(offset: LedgerOffset): Source[Contract[M.AttachmentProposal], NotUsed] =
    clientUtil.subscribeSource(party, offset)
    .flatMapConcat { tx =>
      Source(
        DecodeUtil.decodeCreated[M.AttachmentProposal](tx).toStream
      )
    }

  val proposals: Source[Contract[M.AttachmentProposal], NotUsed] =
    Source.fromFuture(snapshot)
        .flatMapConcat { case (offset, snapshotProps) =>
            Source.combine(
              Source(snapshotProps),
              newProposals(offset))(Concat(_))
        }
}
