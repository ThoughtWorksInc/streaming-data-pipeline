package com.free2wheelers.apps

import com.free2wheelers.apps.StationStatusTransformation.{informationJson2DF, statusJson2DF}
import org.apache.spark.sql.SparkSession

object StationApp {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val stationInformationDF = spark
      .read
      .parquet("file:///Users/Thoughtworks/workspace/DataEng/streaming-data-pipeline/data/raw")
      .transform(informationJson2DF)
      .cache()

    val dataframe = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "station_status")
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(statusJson2DF)
      .join(stationInformationDF, "station_id")
      .writeStream
      .outputMode("append")
      .format("csv")
      .option("header", true)
      .option("truncate", false)
      .option("checkpointLocation", "file:///Users/Thoughtworks/workspace/DataEng/streaming-data-pipeline/data/checkpoint")
      .option("path", "file:///Users/Thoughtworks/workspace/DataEng/streaming-data-pipeline/data/output")
      .start()
      .awaitTermination()
  }
}
