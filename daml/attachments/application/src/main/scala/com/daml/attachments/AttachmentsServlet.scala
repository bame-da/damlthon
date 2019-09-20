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
import org.scalatra.scalate.ScalateSupport
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

  def statsHtml(party: P.Party): String =
    s"""
      |  <html>
      |    <head>
      |    <script src="https://cdn.jsdelivr.net/npm/chart.js@2.8.0"></script>
      |    </head>
      |    <script>
      |    var data = [];
      |    var labels = [];
      |    function randomColor() {
      |      var red = Math.floor(Math.random() * 255);
      |      var green = Math.floor(Math.random() * 255);
      |      var blue = Math.floor(Math.random() * 255);
      |      return 'rgb(' + red + ', ' + green + ', ' + blue + ')'
      |    }
      |    var chart = null;
      |    var nPushed = 0;
      |    function setup() {
      |      var ctx = document.getElementById('chart').getContext('2d');
      |      var d = new Date();
      |      chart = new Chart(ctx, {
      |           // The type of chart we want to create
      |           type: 'line',
      |           data: {
      |               labels: [d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()],
      |               datasets: [
      |                 {
      |                   label: 'Megabytes',
      |                   //backgroundColor: randomColor(),
      |                   borderColor: randomColor(),
      |                   data: []
      |                 },
      |                 {
      |                   label: 'Files',
      |                   //backgroundColor: randomColor(),
      |                   borderColor: randomColor(),
      |                   data: []
      |                 },
      |               ]
      |           },
      |           options: {
      |             animation: { duration: 0 }
      |           }
      |      } )
      |      setInterval(update, 1000);
      |    }
      |    function update() {
      |      var req = new XMLHttpRequest();
      |      req.onreadystatechange = function() {
      |        var d = new Date();
      |        if (req.readyState === XMLHttpRequest.DONE && req.status === 200) {
      |          var r = JSON.parse(req.responseText);
      |          nPushed++;
      |          var shift = nPushed > 25;
      |          if (shift) {
      |            chart.data.datasets[0].data.shift();
      |            chart.data.datasets[1].data.shift();
      |            chart.data.labels.shift();
      |          }
      |          chart.data.datasets[0].data.push(r.bytes / 1024 / 1024);
      |          chart.data.datasets[1].data.push(r.size);
      |          chart.update();
      |          chart.data.labels.push(d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds())
      |        }
      |      }
      |      req.open('GET', 'http://' + window.location.host + '/stats.json', true);
      |      req.send();
      |    }
      |    </script>
      |    <body onload="setup()">
      |    <h2>Files served by $party</h2>
      |    <canvas id="chart"></canvas>
      |    </body>
      |    </html>
      |""".stripMargin
}

class AttachmentsServlet extends ScalatraServlet with FileUploadSupport with JacksonJsonSupport with FutureSupport with ScalateSupport {
  private val applicationId = ApplicationId("AttachmentsServlet")
  val setup = Setup(applicationId)
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

  get("/stats.json") {
    Ok(Map(
      "size" -> AttachmentStore.size,
      "bytes" -> AttachmentStore.bytes
    ))
  }

  get("/stats") {
    contentType="text/html"
    AttachmentsServlet.statsHtml(setup.party)
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
