package com.free2wheelers.apps
import java.util.concurrent.TimeUnit

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}

object FileChecker {
  def main(args: Array[String]): Unit = {

    var LIMIT = 5

    val outputFile = args(0)

    val spark = SparkSession.builder.appName("FileCheckerApp").getOrCreate()

    val hdfs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    if (!hdfs.exists(new Path(outputFile))) {
      throw new RuntimeException("File does not exist")
    }

    val status = hdfs.getFileStatus(new Path(outputFile))

    val duration = System.currentTimeMillis() - status.getModificationTime()
    val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration)

    System.out.println("Diff in Minutes:"+diffInMinutes)

    if (diffInMinutes>LIMIT) {
      throw new RuntimeException("File is late")
    }

    val stationMartDF = spark.read.option("header", "true").csv(outputFile)

    try {
      check_duplicated_stations(stationMartDF)
      check_invalid_lat(stationMartDF, spark)
      check_invalid_long(stationMartDF,spark)

    } catch {
      case analysis_exception: AnalysisException => {
        analysis_exception.printStackTrace()
        throw new RuntimeException("Invalid file")
      }
    }
  }

  private def check_invalid_long(df: DataFrame, spark: SparkSession) = {
    import spark.implicits._

    val nullLatitudeDF = df.filter($"longitude".rlike("\\d+\\.\\d+"))

    if (nullLatitudeDF.count().toInt != df.count().toInt ) {
      throw new RuntimeException("Record with invalid longitude")
    }

  }

  private def check_invalid_lat(df: DataFrame, spark: SparkSession) = {
    import spark.implicits._
    val nullLatitudeDF = df.filter($"latitude".rlike("\\d+\\.\\d+"))

    if (nullLatitudeDF.count().toInt != df.count().toInt ) {
      throw new RuntimeException("Record with invalid latitude")
    }

  }

  private def check_duplicated_stations(stationMartDF: DataFrame) = {
    val maxStationDuplicatesDF = stationMartDF.groupBy("station_id").agg(count("*") as "total").agg(max("total"))
    val maxDuplicates = maxStationDuplicatesDF.first()

    if (maxDuplicates.isNullAt(0)) {
      throw new RuntimeException("Empty file")
    }

    System.out.println("Max duplicates:" + maxDuplicates)
    if (maxDuplicates.getLong(0) > 1.0) {
      throw new RuntimeException("Duplicate station ids")
    }
  }
}
