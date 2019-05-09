package com.free2wheelers.apps

import com.free2wheelers.apps.FileUtil._
import org.apache.spark.sql.{DataFrame, SparkSession}


object Monitor {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder.master("local").appName("Monitoring output").getOrCreate()
    val directory = "src/main/resources/"
    val csvPath = getCSVPathFromDirectory(directory)
    println(csvPath)
    var latestFileModificationTime = 0.0
    var lastFileModificationTime = 0.0
    do {
      lastFileModificationTime = latestFileModificationTime

      Thread.sleep(10000)

      latestFileModificationTime = getLastModifiedFileStatus(spark, csvPath)

      val csvDF: DataFrame =
        spark
          .read
          .option("inferSchema", true)
          .option("header", true)
          .csv(csvPath)
      spark.sparkContext.setLogLevel("ERROR")

      val isLongitudeOrLatitudeNull: Boolean = MonitorUtils.isLongitudeOrLatitudeNull(csvDF)

      val isStationIdUnique: Boolean = MonitorUtils.isStationIdUnique(csvDF)

      if (isStationIdUnique) println("duplicate station ids")
      else println("no duplicate station ids")

      if (isLongitudeOrLatitudeNull) println("latitude / longitude is null")
      else println("latitude / longitude is not null")

      if (latestFileModificationTime == lastFileModificationTime) println("file not modified")
      else println("file modified")

      println("sleeping for 10 seconds")

    } while (true)

  }

}
