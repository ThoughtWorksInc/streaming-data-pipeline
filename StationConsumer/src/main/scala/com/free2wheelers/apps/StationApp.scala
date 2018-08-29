package com.free2wheelers.apps

import java.sql.Timestamp

import com.free2wheelers.apps.StationStatusTransformation.{informationJson2DF, statusJson2DF}
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{Dataset, DataFrame, RelationalGroupedDataset, SparkSession}
//import org.apache.spark.sql.functions.{row_number}
//import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._

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
      .transform( df => informationJson2DF(df, spark) )

    val stationStatusDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(statusJson2DF)

    val dataframe = transformWithTimeWindow(spark, stationStatusDF, stationInformationDF)
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

  def transformWithWindowSpec(spark: SparkSession, stationStatusDF: DataFrame, stationInformationDF: DataFrame): DataFrame = {
    import spark.implicits._
    val windowSpec = Window.partitionBy($"station_id").orderBy($"last_updated".desc)

    stationStatusDF.withColumn("rn", row_number.over(windowSpec))
      .where($"rn" === 1)
      .drop("rn")
      .join(stationInformationDF, "station_id")
  }

  def transformWithTimeWindow(spark: SparkSession, stationStatusDF: DataFrame, stationInformationDF: DataFrame): DataFrame = {
    import spark.implicits._

    val stationStatusWindowDF = stationStatusDF
      .withColumn("timestamp", to_timestamp(from_unixtime(($"last_updated"))))
      .withWatermark("timestamp", "10 minutes")
      .groupBy(
        window($"timestamp", "10 minutes", "6 minutes"),
        $"station_id")
      .agg(max("last_updated") as "last_updated")
      .join(stationStatusDF, Seq("station_id", "last_updated"))

    stationStatusWindowDF.join(stationStatusDF, Seq("station_id", "last_updated"))
      .join(stationInformationDF, "station_id")
      .drop("window")
  }
}
