import org.apache.spark.{SparkConf, SparkContext}

trait SparkContextSetup {
  def withSparkContext(testMethod: (SparkContext) => Any) {
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("Spark test")
    val sparkContext = new SparkContext(conf)
    try {
      testMethod(sparkContext)
    }
    finally sparkContext.stop()
  }
}