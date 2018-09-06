package com.free2wheelers.apps

import com.free2wheelers.apps.StationInformationTransformation.stationInformationJson2DF
import com.free2wheelers.apps.StationStatusTransformation.stationStatusJson2DF
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.TimestampType

object StationPartitionByTimeApp {
  def getLatestModifiedSubPath(spark: SparkSession, path: String): String = {
    import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
    import java.net.URI

    val configuration = spark.sparkContext.hadoopConfiguration
    val statuses = FileSystem.get(new URI(path), configuration).listStatus(new Path(path))
    val metadataFileName = "_spark_metadata"
    statuses.filter(s => !s.getPath.getName.equalsIgnoreCase(metadataFileName))
      .sorted(Ordering.by((_: FileStatus).getModificationTime).reverse)
      .head.getPath.toString
  }

  def main(args: Array[String]): Unit = {

    val retryPolicy = new ExponentialBackoffRetry(1000, 3)

    val zookeeperConnectionString = if (args.isEmpty) "zookeeper:2181" else args(0)

    val zkClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy)

    zkClient.start()

    val kafkaBrokers = new String(zkClient.getData.forPath("/free2wheelers/stationStatus/kafkaBrokers"))

    val topic = new String(zkClient.getData.watched.forPath("/free2wheelers/stationStatus/topic"))

    val stationInfoLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/stationInformation/dataLocation"))

    val checkpointLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/checkpointLocation"))

    val outputLocation = new String(
      zkClient.getData.watched.forPath("/free2wheelers/output/dataLocation"))

    val spark = SparkSession.builder
      .appName("StationConsumer")
      .getOrCreate()

    import spark.implicits._
    val windowSpec = Window.partitionBy($"station_id").orderBy($"last_updated".desc)
    val stationInformationDF = spark
      .read
      .parquet(getLatestModifiedSubPath(spark, stationInfoLocation))
      .transform(df => stationInformationJson2DF(df, spark))
      .dropDuplicates("station_id", "last_updated")
      .withColumn("rn", row_number.over(windowSpec))
      .where($"rn" === 1)
      .drop("rn", "last_updated")

    //if (stationInformationDF.take(1).isEmpty) throw new RuntimeException("No station information for now.")

    val dataframe = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaBrokers)
      .option("subscribe", topic)
      .option("startingOffsets", "latest")
      .load()
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .transform(t => stationStatusJson2DF(t, spark))
      .withColumn("timestamp", $"last_updated" cast TimestampType)
      .withWatermark("timestamp", "60 seconds")
      .dropDuplicates("station_id", "timestamp")
      .drop("timestamp")
      .join(stationInformationDF, "station_id")
      .repartition(1)
      .writeStream
      .partitionBy("last_updated")
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
