val sparkVersion = "2.3.0"

lazy val root = (project in file(".")).

  settings(
    inThisBuild(List(
      organization := "com.free2wheelers",
      scalaVersion := "2.11.8",
      version := "0.0.1"
    )),

    name := "free2wheelers-station-consumer",

    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.apache.kafka" %% "kafka" % "0.10.0.1" % "test",
      "org.apache.curator" % "curator-test" % "2.10.0" % "test",
      "org.apache.spark" %% "spark-core" % sparkVersion,
      "org.apache.spark" %% "spark-sql" % sparkVersion,
      "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
      "org.apache.spark" %% "spark-streaming" % sparkVersion,
      "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion
    )
  )