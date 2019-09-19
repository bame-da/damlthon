import akka.actor.ActorSystem
import com.daml.attachments.AttachmentsServlet
import javax.servlet.ServletContext
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) = {
    context mount (new AttachmentsServlet, "/*")
  }
}