name := "NetworkAnalyzer"

version := "1.0"

scalaVersion := "2.11.8"

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= {
  val akkaV       = "2.5.3"
  val sparkVersion = "2.1.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV,
    "org.apache.spark" %% "spark-core" % sparkVersion,
//    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
//    "org.apache.spark" %% "spark-streaming-twitter" % sparkVersion,
    "org.scalatest"     %% "scalatest" % "3.0.1" % "test",
    "junit" % "junit" % "4.10" % "test"
  )
}