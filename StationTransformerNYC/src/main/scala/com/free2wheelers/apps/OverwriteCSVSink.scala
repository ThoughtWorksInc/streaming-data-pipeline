package com.free2wheelers.apps

import org.apache.spark.sql.execution.streaming.Sink
import org.apache.spark.sql.sources.{DataSourceRegister, StreamSinkProvider}
import org.apache.spark.sql.streaming.OutputMode
import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}


class OverwriteCSVSink(sqlContext: SQLContext,
                       parameters: Map[String, String],
                       partitionColumns: Seq[String],
                       outputMode: OutputMode) extends Sink {

  override def addBatch(batchId: Long, data: DataFrame): Unit = {

    data.sparkSession.createDataFrame(
      data.sparkSession.sparkContext.parallelize(data.collect()), data.schema)
      .repartition(1)
      .write
      .mode(SaveMode.Overwrite)
      .format("csv")
      .option("header", parameters.get("header").orNull)
      .option("truncate", parameters.get("truncate").orNull)
      .option("checkpointLocation", parameters.get("checkpointLocation").orNull)
      .option("path", parameters.get("path").orNull)
      .save()
  }
}

class SinkProvider extends StreamSinkProvider
  with DataSourceRegister {
  override def createSink(
                           sqlContext: SQLContext,
                           parameters: Map[String, String],
                           partitionColumns: Seq[String],
                           outputMode: OutputMode): Sink = {
    new OverwriteCSVSink(sqlContext, parameters, partitionColumns, outputMode)
  }

  override def shortName(): String = "overwriteCSV"
}
