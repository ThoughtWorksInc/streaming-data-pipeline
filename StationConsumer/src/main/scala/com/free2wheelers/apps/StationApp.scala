package com.free2wheelers.apps

import com.free2wheelers.apps.StationStatusTransformation._
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object StationApp {

  def main(args: Array[String]): Unit = {
    val logger = LoggerFactory.getLogger(StationApp.getClass)

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val stationKafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val nycStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataNYC/topic"))
    val sfStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataSF/topic"))
    val franceStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataFrance/topic"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/checkpointLocation"))

    val outputLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/dataLocation"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()
    logger.info("Starting Consumer")
    import spark.implicits._

    val nycStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", nycStationTopic)
      .option("startingOffsets", "latest")
      .option("group.id", "stationConsumerNY")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(nycStationStatusJson2DF(_, spark))

    val sfStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", sfStationTopic)
      .option("startingOffsets", "latest")
      .option("group.id", "stationConsumerSF")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(sfStationStatusJson2DF(_, spark))

    val franceStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", franceStationTopic)
      .option("startingOffsets", "latest")
      .option("group.id", "stationConsumerFrance")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(franceStationStatusJson2DF(_, spark))

    logger.info("Consuming...")

    nycStationDF
      .union(franceStationDF)
      .union(sfStationDF)
      .as[StationStatus]
      .groupByKey(r=>r.station_id)
      .reduceGroups((r1,r2)=>if (r1.last_updated > r2.last_updated) r1 else r2)
      .map(_._2)
      .writeStream
      .format("overwriteCSV")
      .outputMode("complete")
      .option("header", true)
      .option("truncate", false)
      .option("checkpointLocation", checkpointLocation)
      .option("path", outputLocation)
      .start()
      .awaitTermination()
  }
}