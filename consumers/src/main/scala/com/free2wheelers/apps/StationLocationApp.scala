package com.free2wheelers.apps

import com.free2wheelers.apps.StrationStatusTransformation.{denormalizeStream, json2DF}
import org.apache.spark.sql.SparkSession

object StationLocationApp {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val denormalizedDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "station_information")
      .option("startingOffsets", "latest")
      .load()
      .transform(json2DF(StationInformationSchema.schema))
      .transform(denormalizeStream)
      .writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", "file:///Users/alpandy/dev-eng/data/stationinformationdata")
      .start()
  }
}
