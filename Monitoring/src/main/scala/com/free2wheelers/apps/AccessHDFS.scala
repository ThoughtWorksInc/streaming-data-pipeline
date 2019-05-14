package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession

object AccessHDFS extends App {

  val spark = SparkSession.builder.master("local").appName("Monitoring output").getOrCreate()

  while (true) {
    try {
      val count = spark.read.csv("hdfs://hadoop:9000/free2wheelers/stationMart/data").toDF().count()
      println(s"count = $count")
    } catch {
      case e: Exception => println(e.getMessage)
    }

    spark.stop()

  }
}