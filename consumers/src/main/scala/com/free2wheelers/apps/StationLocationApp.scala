package com.free2wheelers.apps

import com.free2wheelers.apps.StrationStatusTransformation.{
  denormalizeStream,
  json2DF
}
import org.apache.spark.sql.SparkSession
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry

object StationLocationApp {
  def main(args: Array[String]): Unit = {

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)
    val client =
      CuratorFrameworkFactory.newClient("localhost:2181", retryPolicy)
    client.start
    val kafkaBrokers = new String(
      client.getData.forPath("/free2wheelers/statusinformation/kafka-brokers"))
    println("DEBUG 111111 "+kafkaBrokers)
    val topic = new String(
      client.getData.watched.forPath("/free2wheelers/statusinformation/topic"))
    println("DEBUG 222222 "+topic)
    val checkpointLocation = new String(
      client.getData.watched
        .forPath("/free2wheelers/statusinformation/checkpointLocation"))
    val dataLocation = new String(
      client.getData.watched
        .forPath("/free2wheelers/statusinformation/dataLocation"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val denormalizedDF = spark.readStream
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
