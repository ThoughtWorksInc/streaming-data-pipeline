package com.free2wheelers.apps

import org.apache.spark.sql.{DataFrame, SparkSession}


object Monitor {

  def main(args: Array[String]): Unit = {

    val sleepDurationSeconds = 60
    val spark = SparkSession.builder.master("local").appName("Monitoring output").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    //val directory = "/free2wheelers/stationMart/data"
    //val csvPath = getCSVPathFromDirectory(directory)

    //var latestFileModificationTime = 0.0
    //var lastFileModificationTime = 0.0

    do {

      try {

        val csvDF: DataFrame =
          spark
            .read
            .option("inferSchema", false)
            .option("header", true)
            .csv("hdfs://hadoop:9000/free2wheelers/stationMart/data")

        //        latestFileModificationTime = getLastModifiedFileStatus(spark, directory)
        val isLongitudeOrLatitudeNull: Boolean = MonitorUtils.isLongitudeOrLatitudeNull(csvDF)

        val isStationIdUnique: Boolean = MonitorUtils.isStationIdUnique(csvDF)

        if (isStationIdUnique) println("\t->duplicate station ids")
        else println("\t-> no duplicate station ids")

        if (isLongitudeOrLatitudeNull) println("\t->latitude / longitude is null")
        else println("\t-> latitude / longitude is not null")

        //        if (latestFileModificationTime == lastFileModificationTime) println("file not modified")
        //        else println("file modified")

        //        println("last file time = " + lastFileModificationTime)
        //        println("latest file time = " + latestFileModificationTime)

        //        lastFileModificationTime = latestFileModificationTime
        Thread.sleep(sleepDurationSeconds * 1000)

        println(s"sleeping for ${sleepDurationSeconds} seconds")

      } catch {
        case e: Exception => println(e.getMessage)
      }

    } while (true)

  }

}
