package com.free2wheelers.apps

import com.free2wheelers.apps.StationInformationTransformation.stationInformationJson2DF
import com.free2wheelers.apps.StationStatusTransformation._
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType

object StationApp {

  def main(args: Array[String]): Unit = {

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val kafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val stationStatusTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationStatus/topic"))

    val stationInformationTopic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationInformation/topic"))

    val stationDataNYC = new String(zkClient.getData.watched.forPath("/free2wheelers/stationDataNYC/topic"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/stationDataNYC/checkpointLocation"))

    val spark = SparkSession.builder
      .appName("NewYorkStationTransformer")
      .getOrCreate()

    import spark.implicits._

    val stationInformationDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", stationInformationTopic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(stationInformationJson2DF(_, spark))
      .withColumn("timestamp", $"last_updated" cast TimestampType)
      .drop("last_updated")
      .withWatermark("timestamp", "90 seconds")

    val stationStatusDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", stationStatusTopic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(stationStatusJson2DF(_, spark))
      .withColumn("timestamp", $"last_updated" cast TimestampType)
      .withWatermark("timestamp", "30 seconds")

    val stationDataDF = stationStatusDF
      .join(stationInformationDF
        .withColumnRenamed("station_id", "i_station_id")
        .withColumnRenamed("timestamp", "i_timestamp")
        , expr(
          """
            |station_id=i_station_id AND
            |timestamp <= i_timestamp + interval 90 seconds  AND
            |timestamp >= i_timestamp
          """.stripMargin),
        "left_outer")
      .filter($"name".isNotNull)
      .as[StationData]
      .groupByKey(r => r.station_id)
      .reduceGroups((r1, r2) => if (r1.timestamp.after(r2.timestamp)) r1 else r2)
      .map(_._2)
      .drop("timestamp")
      .orderBy($"station_id")

    stationDataDF
      .toJSON
      .selectExpr("CAST(value AS STRING)")
      .writeStream
      .format("kafka")
      .outputMode("complete")
      .option("checkpointLocation", checkpointLocation)
      .option("topic", stationDataNYC)
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .start()
      .awaitTermination()
  }
}