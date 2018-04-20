package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import StrationStatusTransformation.{json2DF,denormalizeStream}

object StationApp {
  val spark = SparkSession.builder
    .appName("StationConsumer")
    .getOrCreate()

  val denormalizedDF = spark.readStream
    .format("kafka")
    .option("kafka.bootstrap.servers", "localhost:9092")
    .option("subscribe", "station_status")
    .option("startingOffsets", "latest")
    .load()
    .transform(json2DF)
    .transform(denormalizeStream)

}
