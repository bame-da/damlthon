package com.daml.attachments

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}
import org.scalatra.{BadRequest, Ok, ScalatraServlet}
import org.slf4j.LoggerFactory

object AttachmentsServlet {
  def attachmentUrl(serverPort: Int, hash: String): String = {
    val extIp = System.getenv("EXTERNAL_IP")
    "http://" + (if (extIp == null) "localhost" else extIp) + ":" + serverPort + "/attachments/" + hash
  }
}

class AttachmentsServlet extends ScalatraServlet with FileUploadSupport with JacksonJsonSupport {
  private val logger = LoggerFactory.getLogger(this.getClass)

  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  configureMultipartHandling(MultipartConfig(maxFileSize = Some(10*1024*1024)))
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

  post("/upload") {
    val attachment = fileParams("attachment").get()
    val hash = AttachmentStore.insert(fileParams("attachment").get())
    logger.info(s"upload: accepted file with hash $hash")

    Ok(Map(
      "hash" -> hash,
      "url" -> AttachmentsServlet.attachmentUrl(serverPort, hash)))
  }
}
