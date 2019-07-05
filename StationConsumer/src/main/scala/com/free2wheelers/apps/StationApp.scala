package com.free2wheelers.apps

import java.time.Instant
import java.time.format.{DateTimeFormatter, DateTimeParseException}

import com.free2wheelers.apps.StationTransformer._
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.{Dataset, Row, SparkSession}
import org.apache.log4j.{Level, LogManager, Logger}

object StationApp {
  var log: Logger = LogManager.getRootLogger
  log.setLevel(Level.INFO)

  def main(args: Array[String]): Unit = {

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val stationKafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val nycStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataNYC/topic"))
    val nycV2StationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataNYCV2/topic"))
    val sfStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataSF/topic"))
    val marseilleStationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataMarseille/topic"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/checkpointLocation"))

    val outputLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/dataLocation"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    val nycV2DF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("auto.offset.reset","latest")
      .option("subscribe", nycV2StationTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss","false")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(sfStationStatusJson2DF(_, spark))

    val sfStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("auto.offset.reset","latest")
      .option("subscribe", sfStationTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss","false")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(sfStationStatusJson2DF(_, spark))

    val marseilleStationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", stationKafkaBrokers)
      .option("subscribe", marseilleStationTopic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss","false")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(marseilleStationStatusJson2DF(_, spark))

    val version2DF = sfStationDF.union(marseilleStationDF).union(nycV2DF)

    unionStationData(version2DF, spark)
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

  def parseDateTimeToIsoFormat(stationInfo: StationStatus) = {
    try {
      val originalDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
      val isoDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
      val parsedLastUpdated = originalDateTimeFormat.parse(stationInfo.last_updated)

      stationInfo.copy(last_updated = isoDateTimeFormat.format(parsedLastUpdated))
    } catch {
      case ex: DateTimeParseException => {
        log.error(s"Station Id: ${stationInfo.station_id} | " +
          s"The last_updated date ${stationInfo.last_updated} is not in this " +
          s"format - yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        stationInfo.copy(last_updated = "")
      }
    }

  }

  def unionStationData(version2DF: Dataset[Row], spark: SparkSession): Dataset[StationStatus] = {
    import spark.implicits._
    version2DF
      .as[StationStatus]
      .groupByKey(row => (row.latitude, row.longitude))
      .reduceGroups((row1, row2) => {
        val time1 = Instant.parse(row1.last_updated)
        val time2 = Instant.parse(row2.last_updated)
        if (time1.isAfter(time2)) row1 else row2
      })
      .map(_._2)
      .map(parseDateTimeToIsoFormat)
      .filter(row => !"".equals(row.last_updated))
  }
}