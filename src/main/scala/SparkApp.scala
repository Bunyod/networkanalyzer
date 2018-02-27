import java.io.File
import java.util.Date

import Entities._
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, FileUtil, Path}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object SparkApp extends App {

  val conf = new SparkConf().setMaster("local").setAppName("Prevent multiple logins")
  val sc = new SparkContext(conf)

  val resultPath = "result.csv"
  val destinationFile = s"ResultBySpark${new Date().getTime}.csv"

  val inputData = sc.textFile(FilePath)
  val loginsRdd: RDD[Login] = inputData.map(Login.parse)

  makeIndex(loginsRdd).saveAsTextFile(resultPath)
  merge(resultPath, destinationFile)

  FileUtil.fullyDelete(new File(resultPath))
  FileUtil.fullyDelete(new File(s".$destinationFile.crc"))

  def merge(srcPath: String, dstPath: String): Unit = {
    val hadoopConfig = new Configuration()
    val hdfs = FileSystem.get(hadoopConfig)
    FileUtil.copyMerge(hdfs, new Path(srcPath), hdfs, new Path(dstPath), false, hadoopConfig, null)
  }

  def makeIndex(logins: RDD[Login]): RDD[String] = {
    logins
      .groupBy(_.ip)
      .map { entry => convertToOuputLogin(entry._2.toSeq) }
      .filter(_.isDefined)
      //    .sortBy(_.map(_.ipAddress).getOrElse(""))
      .map { s =>
      if (s.exists(_.logins.nonEmpty)) s.get.toString
      else ""
    }
  }

}