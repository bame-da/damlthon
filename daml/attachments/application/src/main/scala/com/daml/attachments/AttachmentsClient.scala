package com.daml.attachments

import com.softwaremill.sttp._
import com.softwaremill.sttp.json4s._

import scala.util.{Failure, Success, Try}

object AttachmentsClient {

  case class AttachmentUploadResponse(hash: String, url: String)

  implicit val serialization =  org.json4s.native.Serialization
  implicit val backend = HttpURLConnectionBackend()

  def downloadAttachment(uri: String): Either[String, Array[Byte]] =
    Uri.parse(uri) match {
      case Failure(_) => Left(s"Bad uri: $uri")
      case Success(u) =>
        Try(
          sttp.get(u)
            .response(asByteArray)
            .send()
            .body
        ).fold(ex => Left(ex.toString), identity)
    }

  def uploadAttachment(host: String, port: Int, bytes: Array[Byte]): AttachmentUploadResponse = {
    sttp.post(Uri(host, port, List("upload")))
      .multipartBody(
        multipart("attachment", bytes)
          .fileName("attachment")
          .contentType("application/octet-stream")
      )
      .response(asJson[AttachmentUploadResponse])
      .send()
      .unsafeBody
  }

  def roundtripTest(host: String, port: Int, bytes: Array[Byte]): Unit = {
    val upResp = uploadAttachment(host, port, bytes)
    println(s"roundtripTest: Downloading from ${upResp.url}...")
    val downResp = downloadAttachment(upResp.url)
    assert(bytes sameElements downResp.right.get)
  }

}
