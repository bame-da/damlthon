package com.daml.attachments

import org.json4s.{DefaultFormats, Formats}
import org.scalatra.json.JacksonJsonSupport
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig}
import org.scalatra.{BadRequest, Ok, ScalatraServlet}

class AttachmentsServlet extends ScalatraServlet with FileUploadSupport with JacksonJsonSupport {
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
    val extIp = System.getenv("EXTERNAL_IP")
    val url = "http://" + (if (extIp == null) "localhost" else extIp) + ":" + serverPort + "/attachments/" + hash
    Ok(Map(
      "hash" -> hash,
      "url" -> url))
  }
}
