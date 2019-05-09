package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession

object AccessHDFS extends App {

  val spark = SparkSession.builder.master("local").appName("Monitoring output").getOrCreate()

  val count = spark.read.csv("hdfs://hadoop:9000/free2wheelers/stationMart/data").toDF().count()
  println("count is = " + count)

  while(true){
    println("monitoring..")
  }

  spark.stop()

}