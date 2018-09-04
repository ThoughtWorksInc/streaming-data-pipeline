package com.free2wheelers.apps

import com.free2wheelers.apps.StationInformationTransformation.stationInformationJson2DF
import com.free2wheelers.apps.StationStatusTransformation.stationStatusJson2DF
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.SparkSession

object StationApp {
  def main(args: Array[String]): Unit = {

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val kafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val topic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationStatus/topic"))

    //TODO: change this to use the latest location when it's available
    val latestStationInfoLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/stationInformation/dataLocation"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/checkpointLocation"))

    val outputLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/dataLocation"))
    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val stationInformationDF = spark
      .read
      .parquet(latestStationInfoLocation)
      .transform(df => stationInformationJson2DF(df, spark))
      .dropDuplicates("station_id", "last_updated")
      .drop("last_updated")

    if (stationInformationDF.take(1).isEmpty) throw new RuntimeException("No station information for now.")

    val dataframe = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(t => stationStatusJson2DF(t, spark))
      .join(stationInformationDF, "station_id")
      .repartition(1)
      .writeStream
      .outputMode("append")
      .format("csv")
      .option("header", true)
      .option("truncate", false)
      .option("checkpointLocation", checkpointLocation)
      .option("path", outputLocation)
      .start()
      .awaitTermination()
  }
}
