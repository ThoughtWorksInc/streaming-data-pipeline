package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, Matchers}

class FileCheckerTest extends FeatureSpec with Matchers {

    feature("Check file created in last 5 mins") {
        val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
        scenario("file should exist") {
//            checkFileExists()
        }
    }

}
