package com.daml.attachments
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

object JettyLauncher {
  def launch(port: Int): Unit = {
    println(s"Attachments server listening on port $port")

    val server = new Server(port)
    val context = new WebAppContext()
    context setContextPath "/"
    val resourceBase = "/non-existing"
    context.setResourceBase(resourceBase)

    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)
    server.start()
    server.join()
  }
}
