import org.scalatra.LifeCycle
import javax.servlet.ServletContext
import com.daml.attachments.AttachmentsServlet

class ScalatraBootstrap extends LifeCycle {
  override def init(context: ServletContext) = {
    context mount (new AttachmentsServlet, "/*")
  }
}