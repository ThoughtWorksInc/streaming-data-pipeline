package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry

object StationLocationApp {
  def main(args: Array[String]): Unit = {

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)
    val zkClient = CuratorFrameworkFactory.newClient("zookeeper:2181", retryPolicy)

    zkClient.start

    val kafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationInformation/kafkaBrokers"))

    val topic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationInformation/topic"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/stationInformation/checkpointLocation"))

    val dataLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/stationInformation/dataLocation"))

    val spark = SparkSession.builder
      .appName("RawDataSaver")
      .getOrCreate()

    val savedStream = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .writeStream
      .outputMode("append")
      .format("parquet")
      .option("checkpointLocation", checkpointLocation)
      .option("path", dataLocation)
      .start()
      .awaitTermination()
  }
}
