import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object StreamingAppRunner extends LoginsAnalyzer {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("logins-system")
    implicit val materializer = ActorMaterializer()
    println("Running...")
    val result = findMultipleLoginSeries(Entities.FilePath)

    import system.dispatcher
    result.onComplete { _ =>
      println("Stopped")
      system.terminate()
    }
  }

}
