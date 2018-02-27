import Entities._
import org.scalatest._

class SparkAppSpec extends WordSpec
                      with Matchers
                      with SparkContextSetup {

  val filePath = "src/test/resources/logins.csv"

  val logins = List(
    Login("angus123", "210.181.101.170", "2015-12-02 15:17:39"),
    Login("angus123", "210.181.101.170", "2015-12-03 14:07:51"),
    Login("angus123", "210.181.101.170", "2015-12-03 14:27:16"),
    Login("angus123", "210.181.101.170", "2015-12-03 14:27:50"),
    Login("angus123", "210.181.101.170", "2015-12-03 14:33:08"),
    Login("angus123", "210.181.101.170", "2015-12-05 15:54:10")
  )

  "There are 4 entries during 60 min" should {
    "return successful" in withSparkContext { (sparkContext) =>
      val loginsRDD = sparkContext.parallelize(logins)
      val res = SparkApp.makeIndex(loginsRDD).collect()
      val assertionRes = res.head.split("\n").length == 4
      assert(assertionRes, "result should equal to 4")
    }
  }

  "Checking with csv file" should {
    "be successful" in withSparkContext { (sparkContext) =>
      val inputData = sparkContext.textFile(filePath)
      val loginsRDD = inputData.map(Login.parse)
      assert(loginsRDD.collect().length == 34)
      val res = SparkApp.makeIndex(loginsRDD).collect()
      val assertionRes = res.head.split("\n").length == 2
      assert(assertionRes)
    }
  }

}