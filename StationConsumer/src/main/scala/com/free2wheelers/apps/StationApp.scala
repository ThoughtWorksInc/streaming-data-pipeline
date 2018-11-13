package com.free2wheelers.apps

import java.time.Instant

import com.free2wheelers.apps.StationStatusTransformation._
import com.typesafe.scalalogging.Logger
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object StationApp {

  val logger = Logger("StationConsumer")

  def main(args: Array[String]): Unit = {

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val stationKafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val nycStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataNYC/topic"))
    val sfStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataSF/topic"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/checkpointLocation"))

    val outputLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/dataLocation"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val nycStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", nycStationTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss","false")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(nycStationStatusJson2DF(_, spark))

    logger.info("NYC")

    val sfStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", sfStationTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss","false")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(sfStationStatusJson2DF(_, spark))

    logger.info("SF")

    unionStationData(nycStationDF, sfStationDF, spark)
      .writeStream
      .format("overwriteCSV")
      .outputMode("complete")
      .option("header", true)
      .option("truncate", false)
      .option("checkpointLocation", checkpointLocation)
      .option("path", outputLocation)
      .start()
      .awaitTermination()

    logger.info("File written")
  }

  def unionStationData(nycStationDF: Dataset[Row], sfStationDF: Dataset[Row], spark: SparkSession): Dataset[StationStatus] = {
    import spark.implicits._

    val unionDF = nycStationDF
      .union(sfStationDF)
      .as[StationStatus]
      .groupByKey(row => row.station_id)
      .reduceGroups((row1, row2) => {
        val time1 = Instant.parse(row1.last_updated)
        val time2 = Instant.parse(row2.last_updated)
        if (time1.isAfter(time2)) row1 else row2
      })
      .map(_._2)

    logger.info("Union")

    return unionDF
  }
}