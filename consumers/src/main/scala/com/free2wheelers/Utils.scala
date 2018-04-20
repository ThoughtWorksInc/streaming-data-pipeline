package com.free2wheelers

import org.apache.spark.sql.{DataFrame, SparkSession}

object Utils {
  def createDataFrameFromKafkaStationStatusTopic(sparkSession: SparkSession, kafkaBrokerConfig: String): DataFrame = {
    sparkSession.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokerConfig)
      .option("subscribe", "station_status")
      .load()
  }
}
