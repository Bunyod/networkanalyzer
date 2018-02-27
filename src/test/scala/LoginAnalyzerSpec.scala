import Entities._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpecLike}
import org.scalatest.Assertions._

class LoginAnalyzerSpec extends TestKit(ActorSystem("TestActorSystem"))
                          with WordSpecLike
                          with Matchers
                          with ScalaFutures
                          with LoginsAnalyzer {

  implicit val materializer = ActorMaterializer()
  val filePath = "src/test/resources/logins.csv"

  "Result should be successful" in {
    val result = findMultipleLoginSeries(filePath)
    whenReady(result) { res =>
      assert(res.wasSuccessful)
    }
  }

  "There are 4 entries during 60 min" in {
    val lst = List(
      Login("angus123","210.181.101.170","2015-12-02 15:17:39"),
      Login("angus123","210.181.101.170","2015-12-03 14:07:51"),
      Login("angus123","210.181.101.170","2015-12-03 14:27:16"),
      Login("angus123","210.181.101.170","2015-12-03 14:27:50"),
      Login("angus123","210.181.101.170","2015-12-03 14:33:08"),
      Login("angus123","210.181.101.170","2015-12-05 15:54:10")
    )
    val result = convertToOuputLogin(lst)
    println(result)
    result.get.logins.size shouldEqual 4
  }

  "Check to successful parsing" in {
    val rawInput = """"thetoxicgoon","5.81.233.223","2015-12-10 10:34:03"
                |"vladie","89.228.9.198","2015-12-10 10:34:04"
                |"Starshrek","174.135.42.244","2015-12-10 10:34:15"
                |"Gtblaze45","121.219.128.132","2015-12-10 10:34:27"
                |""".stripMargin
    val lst = rawInput.split("\n").toSeq

    val result = lst.map(Login.parse)
    println(result)
    result.head.ip shouldEqual "5.81.233.223"
    result.size shouldEqual 4
  }

  "Check to failed parsing" in {
    val rawInput = """"thetoxicgoon","5.81.233.223","2015-12-10 10:34:03", "asdf"
                |"vladie","89.228.9.198","2015-12-10 10:34:04"
                |"Starshrek","174.135.42.244","2015-12-10 10:34:15"
                |"Gtblaze45","121.219.128.132","2015-12-10 10:34:27"
                |""".stripMargin
    val lst = rawInput.split("\n").toSeq
    println(lst)
    assertThrows[MatchError](lst.map(Login.parse))
  }

}
