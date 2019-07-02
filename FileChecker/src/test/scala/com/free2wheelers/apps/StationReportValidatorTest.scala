package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, Matchers}

class StationReportValidatorTest extends FeatureSpec with Matchers {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()

    import spark.implicits._

    val columns = Array("station_id", "latitude", "longitude")
    val stationReportValidator = new StationReportValidator(spark)

    feature("should validate station location - Latitude") {
        scenario("should return true if all latitudes are correct") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(true)
        }

        scenario("should return false when there are latitude errors") {
            val stationDF = Seq((1, "1.0a", 1.0), (2, "a4.0", 5.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(false)
        }
    }

    feature("should validate station location - Longitude") {
        scenario("should return true if longitudes are correct") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(true)
        }

        scenario("should return false when there are latitude errors") {
            val stationDF = Seq((1, 1.0, "1.0a"), (2, 2.0, "9.0")).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(false)
        }
    }

    feature("should validate station id") {
        scenario("should return true when there are 0 Duplicate Errors") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(true)
        }

        scenario("should return false when there are Duplicate Errors") {
            val stationDF = Seq((1, 1.0, 1.0), (1, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(false)
        }
    }

    feature("should validate if there is data inside the file") {

        scenario("should return false if the file is empty") {
            val stationDF = Seq.empty[(Int, Double, Double)].toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(false)
        }

        scenario("should return true if the file contains data") {
            val stationDF = Seq((1, 1.0, 1.0)).toDF(columns: _*)
            val result = stationReportValidator.isValid(stationDF)
            result should equal(true)
        }

    }
}
