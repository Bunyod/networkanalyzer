import java.nio.file.Paths
import java.util.Date

import Entities._
import akka.actor.ActorSystem
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink}
import akka.stream.{ActorMaterializer, IOResult}
import akka.util.ByteString

import scala.concurrent.Future

trait LoginsAnalyzer {

  def findMultipleLoginSeries(source: String)
                             (implicit system: ActorSystem, materializer: ActorMaterializer): Future[IOResult] = {

    val outputFileName = s"result${new Date().getTime}.csv"
    FileIO.fromPath(Paths.get(source))
      .via(Framing.delimiter(ByteString("\n"), 256, true).map(_.utf8String))
      .map(Login.parse)
      .groupBy(Int.MaxValue, entry => entry.ip)
      .fold(Seq.empty[Login])(_ :+ _)
      .map(convertToOuputLogin)
      .filter(_.isDefined)
      .mergeSubstreams
      .runWith(toCsv(outputFileName))

  }

  def toCsv(fileName: String): Sink[Option[OutputLogin], Future[IOResult]] = {
    Flow[Option[OutputLogin]]
      .map{ s =>
        if (s.isDefined) ByteString(s.get.toString + "\n")
        else ByteString("")
      }
      .toMat(FileIO.toPath(Paths.get(fileName)))(Keep.right)
  }

}
