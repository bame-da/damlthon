package com.daml.attachments

import java.security.MessageDigest

import scala.collection.concurrent.TrieMap

// FIXME(JM): This should be persisting to disk.
object AttachmentStore {
  private val attachments = TrieMap.empty[String, Array[Byte]]
  attachments("test") = Array(1,2,3)

  def insert(data: Array[Byte]): String = {
    // FIXME(JM): double-check data hash?
    val hash = attachmentHash(data)
    attachments(hash) = data
    hash
  }

  def insert(hash: String, data: Array[Byte]): Unit = {
    attachments(hash) = data
  }

  def lookup(hash: String): Option[Array[Byte]] =
    attachments.get(hash)

  def attachmentHash(bytes: Array[Byte]): String = {
    MessageDigest.getInstance("SHA")
      .digest(bytes)
      .map(b => String.format("%02x", Byte.box(b)))
      .mkString("")
  }

  def contains(hash: String): Boolean =
    attachments.contains(hash)


}
