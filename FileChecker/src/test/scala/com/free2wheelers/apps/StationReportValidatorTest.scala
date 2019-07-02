package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, Matchers}

class StationReportValidatorTest extends FeatureSpec with Matchers {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()

    import spark.implicits._

    val columns = Array("station_id", "latitude", "longitude")
    val stationReportValidator = new StationReportValidator(spark)

    feature("should validate station location - Latitude") {
        scenario("should return Latitude Errors as 0 if all latitudes are correct") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Latitude Errors") should be(0)
        }

        scenario("should return number of Latitude Errors that are incorrect") {
            val stationDF = Seq((1, "1.0a", 1.0), (2, "a4.0", 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Latitude Errors") should be(2)
        }
    }

    feature("should validate station location - Longitude") {
        scenario("should return Longitude Errors as 0 if all latitudes are correct") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Longitude Errors") should be(0)
        }

        scenario("should return number of Longitude Errors that are incorrect") {
            val stationDF = Seq((1, 1.0, "1.0a"), (2, 2.0, "9.0")).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Longitude Errors") should be(1)
        }
    }

    feature("should validate station id") {
        scenario("station_id should have 0 Duplicate Errors if all stations are entered correctly") {
            val stationDF = Seq((1, 1.0, 1.0), (2, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Duplicates Errors") should be(0)
        }

        scenario("should return the number of duplicate stations in the file") {
            val stationDF = Seq((1, 1.0, 1.0), (1, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Duplicates Errors") should be(1)
        }
    }

    feature("should validate if there is data inside the file") {

        scenario("should return true if the file is empty") {
            val stationDF = Seq.empty[(Int, Double, Double)].toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Is File Empty") should equal(true)
        }

        scenario("should return false if the file contains data") {
            val stationDF = Seq((1, 1.0, 1.0), (1, 4.0, 5.0)).toDF(columns: _*)
            val result = stationReportValidator.validate(stationDF)
            result("Is File Empty") should equal(false)
        }

    }
}
