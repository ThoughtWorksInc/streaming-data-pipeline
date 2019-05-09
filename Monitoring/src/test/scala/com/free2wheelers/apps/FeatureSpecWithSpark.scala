package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.scalatest.FeatureSpec

class FeatureSpecWithSpark extends FeatureSpec {
  val spark: SparkSession = SparkSession.builder()
    .appName("Test Spark App")
    .config("spark.driver.host", "127.0.0.1")
    .master("local")
    .getOrCreate()
}
