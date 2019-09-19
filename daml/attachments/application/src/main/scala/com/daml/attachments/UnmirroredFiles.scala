package com.daml.attachments

import akka.NotUsed
import akka.stream.Materializer
import akka.stream.scaladsl.{Concat, Source}
import com.daml.attachments.model.{File => M}
import com.digitalasset.ledger.api.refinements.ApiTypes.Party
import com.digitalasset.ledger.api.v1.ledger_offset.LedgerOffset
import com.digitalasset.ledger.client.binding.Contract

import scala.concurrent.Future


/** Unmirrored files provides a stream of `File` contracts for files that are not yet mirrored
 * by the local party.
 */
case class UnmirroredFiles(party: Party, offset0: LedgerOffset, clientUtil: ClientUtil, implicit val amat: Materializer) {
  //private val logger = LoggerFactory.getLogger(this.getClass)

  private def snapshot: Future[(LedgerOffset, Stream[Contract[M.File]])] =
      clientUtil.acsSnapshot(party).runFold(offset0 -> Stream.empty[Contract[M.File]]) { case ((_, props), acsResponse) =>
        LedgerOffset().withAbsolute(acsResponse.offset) ->
          (props ++ acsResponse.activeContracts
            .flatMap { ev => DecodeUtil.decodeCreated[M.File](ev).toSeq })
      }

  // Stream of new proposals after the active contract snapshot
  private def newFiles(offset: LedgerOffset): Source[Contract[M.File], NotUsed] =
    clientUtil.subscribeSource(party, offset)
    .flatMapConcat { tx =>
      Source(
        DecodeUtil.decodeCreated[M.File](tx).toStream
      )
    }

  val files: Source[Contract[M.File], NotUsed] =
    Source.fromFuture(snapshot)
        .flatMapConcat { case (offset, snapshotProps) =>
            Source.combine(
              Source(snapshotProps),
              newFiles(offset))(Concat(_))
        }
        .filter(f => f.value.owner != party && !AttachmentStore.contains(f.value.hash.unpack))
}
