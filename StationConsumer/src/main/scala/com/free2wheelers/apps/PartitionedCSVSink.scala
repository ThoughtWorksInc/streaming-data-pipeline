package com.free2wheelers.apps

import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSinkProvider}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.sql.functions._


class PartitionedCSVSink(sqlContext: SQLContext,
                         parameters: Map[String, String],
                         partitionColumns: Seq[String],
                         outputMode: OutputMode) extends Sink {

  override def addBatch(batchId: Long, data: DataFrame): Unit = {

    data.sparkSession.createDataFrame(
      data.sparkSession.sparkContext.parallelize(data.collect()), data.schema)
      .withColumn("year", year(col("consumption_time")))
      .withColumn("month", month(col("consumption_time")))
      .withColumn("day", dayofmonth(col("consumption_time")))
      .withColumn("hour", hour(col("consumption_time")))
      .withColumn("minute", minute(col("consumption_time")))
      .withColumn("second", second(col("consumption_time")))
      .drop("consumption_time")
      .repartition(1)
      .write
      .mode(SaveMode.Append)
      .format("csv")
      .option("header", parameters.get("header").orNull)
      .option("truncate", parameters.get("truncate").orNull)
      .option("checkpointLocation", parameters.get("checkpointLocation").orNull)
      .option("path", parameters.get("path").orNull)
      .partitionBy("year", "month", "day", "hour", "minute", "second")
      .save
  }
}

class SinkProvider extends StreamSinkProvider
  with DataSourceRegister {
  override def createSink(
                           sqlContext: SQLContext,
                           parameters: Map[String, String],
                           partitionColumns: Seq[String],
                           outputMode: OutputMode): Sink = {
    new PartitionedCSVSink(sqlContext, parameters, partitionColumns, outputMode)
  }

  override def shortName(): String = "appendCSV"
}
