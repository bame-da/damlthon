package com.daml.attachments

import java.net.{URLConnection, URLEncoder}
import java.nio.charset.StandardCharsets
import java.util.Base64

import com.daml.attachments.model.{File => M}
import com.digitalasset.ledger.api.refinements.ApiTypes.{ApplicationId, WorkflowId}
import com.digitalasset.ledger.client.binding.{Primitive => P}
import org.json4s.{DefaultFormats, Formats}
import org.scalatra._
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

//
// FIXME(JM): This file is full of hacks.
//

object AttachmentsServlet {
  def attachmentUrl(serverPort: Int, hash: String): String = {
    val extIp = System.getenv("EXTERNAL_IP")
    "http://" + (if (extIp == null) "localhost" else extIp) + ":" + serverPort + "/attachments/" + hash
  }

  def plainTextUrl(serverPort: Int, hash: String, key: String, iv: String, mimeType: String): String = {
    val extIp = System.getenv("EXTERNAL_IP")
    "http://" + (if (extIp == null) "localhost" else extIp) + ":" + serverPort +
      s"/decrypted/$hash?key=$key&iv=$iv&mimetype=${urlEncode(mimeType)}"
  }

  def urlEncode(s: String): String =
    URLEncoder.encode(s, StandardCharsets.UTF_8.toString).toLowerCase

}

class AttachmentsServlet extends ScalatraServlet with FileUploadSupport with JacksonJsonSupport with FutureSupport {
  private val applicationId = ApplicationId("AttachmentsServlet")
  private val setup = Setup(applicationId)
  implicit val executor: ExecutionContext = setup.ec

  private val logger = LoggerFactory.getLogger(this.getClass)

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(10 * 1024 * 1024)))
  before() {
    contentType = formats("json")
  }

  get("/attachments/:hash") {
    val hash = params("hash")
    AttachmentStore.lookup(hash) match {
      case Some(bytes) =>
        Ok(bytes,
          Map(
            "Content-Type" -> "application/octet-stream",
            "Content-Disposition" -> ("attachment; filename=\"" + hash + "\"")))
      case None =>
        BadRequest
    }
  }

  get("/decrypted/:hash") {
    val hash = params("hash")
    val key = params("key")
    val iv = params("iv")
    val mimeType = params("mimetype")
    AttachmentStore.lookup(hash) match {
      case Some(bytes) =>
        val plainText = decrypt(bytes, unpack64(key), unpack64(iv))
        Ok(plainText,
          Map(
            "Content-Type" -> mimeType
            //"Content-Disposition" -> ("attachment; filename=\"" + hash + "\""))
          )
        )
      case None =>
        BadRequest
    }
  }

  post("/upload") {
    val attachment = fileParams("attachment").get()
    val hash = AttachmentStore.insert(fileParams("attachment").get())
    logger.info(s"upload: accepted file with hash $hash")

    Ok(Map(
      "hash" -> hash,
      "url" -> AttachmentsServlet.attachmentUrl(serverPort, hash)))
  }

  post("/uploadAndSubmit") {
    val filename = params("filename")
    val observers = params("observers").split(',').map(p => P.Party(p.trim))
    val attachment = fileParams("attachment").get()

    val encKey = random16
    val encIV = random16
    val encryptedAttachment = encrypt(attachment, encKey, encIV)
    val hash = AttachmentStore.insert(encryptedAttachment)
    val workflowId = WorkflowId(s"FileUpload${hash}")

    val mimetypeGuess =
      URLConnection.guessContentTypeFromName(filename.toString)
    val mimetype = if (mimetypeGuess == null) "application/octet-stream" else mimetypeGuess

    logger.info(s"upload: accepted file with hash $hash (mimetype = $mimetype)")

    new AsyncResult {
      val is =
        for {
          clientUtil <- setup.clientUtilF

          fileContract = M.File(
            fileName = filename,
            hash = M.SHA256(hash),
            encryption = M.Encryption.EncAES256(pack64(encKey), pack64(encIV)),
            mimeType = M.MimeType(mimetype),
            owner = setup.party,
            observers = observers.toList,
            uri = M.URI(AttachmentsServlet.attachmentUrl(serverPort, hash))
          )

          tx <- clientUtil.submitCommandAndWait(
            setup.party,
            workflowId,
            fileContract.create
          )
        } yield {
          Ok(Map(
            "hash" -> hash,
            "url" -> AttachmentsServlet.attachmentUrl(serverPort, hash),
            "plain_url" ->
              AttachmentsServlet.plainTextUrl(serverPort, hash, pack64(encKey), pack64(encIV), mimetype),

            "contract_id" -> tx.events.head.getCreated.contractId // FIXME
          )
          )
        }
    }
  }

  import javax.crypto.Cipher
  import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

  private val rng = new java.util.Random
  private def random16: Array[Byte] = {
    val key: Array[Byte] = Array.ofDim(16)
    rng.nextBytes(key)
    key
  }
  private val enc64 = Base64.getUrlEncoder
  private val dec64 = Base64.getUrlDecoder
  private def pack64(bytes: Array[Byte]): String = enc64.encodeToString(bytes)
  private def unpack64(s: String): Array[Byte] = dec64.decode(s)

  // FIXME(JM): encode key and iv as base64!
  def encrypt(plainText: Array[Byte], key: Array[Byte], iv: Array[Byte]): Array[Byte] = {
    val ivSpec = new IvParameterSpec(iv)
    val skeySpec = new SecretKeySpec(key, "AES")
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivSpec)
    cipher.doFinal(plainText)
  }

  def decrypt(encrypted: Array[Byte], key: Array[Byte], iv: Array[Byte]): Array[Byte] = {
    val ivSpec = new IvParameterSpec(iv)
    val skeySpec = new SecretKeySpec(key, "AES")
    val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivSpec)
    cipher.doFinal(encrypted)
  }

}
