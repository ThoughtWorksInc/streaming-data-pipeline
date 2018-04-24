package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import com.free2wheelers.apps.StationStatusTransformation.json2DF

object StationApp {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    spark.conf.set("spark.sql.streaming.checkpointLocation", "file:///data")

    val dataframe = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "station_status")
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(json2DF)
      .writeStream
      .outputMode("append")
      .format("csv")
      .option("path", "file:///data")
      .start()
      .awaitTermination()
  }
}
