package com.free2wheelers.apps

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.SparkSession

object FileChecker {
  def main(args: Array[String]): Unit = {
    val outputFile = args(0)
    val spark = SparkSession.builder.appName("FileCheckerApp").getOrCreate()
    val hdfs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    val stationReportValidator = new StationReportValidator(spark)
    val fileValidator = new FileValidator()

    val checker = new ValidationEngine(stationReportValidator, fileValidator)

    val stationMartDF = spark.read.option("header", "true").csv(outputFile)

    checker.checkFile(outputFile, stationMartDF, hdfs)
  }
}
