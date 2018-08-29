package com.free2wheelers.apps

import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}
import com.free2wheelers.apps.StationApp._

class StationAppTest extends FeatureSpec with Matchers with GivenWhenThen {
  feature("Apply transformations to station status and information data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

    scenario("Transform station_status data frame and extract useful fields") {

      Given("Sample data for station_status")
      val statusDF = spark.createDataset(Array(
        ("72", 1, 33, false, true, 1524170881),
        ("73", 3, 44, true, false, 1524170881),
        ("79", 6, 55, true, false, 1524170881),
        ("72", 1, 30, false, true, 1524171541),
        ("73", 3, 40, true, false, 1524171541),
        ("79", 6, 50, true, false, 1524171541),
        ("72", 1, 20, false, true, 1524171781),
        ("73", 3, 30, true, false, 1524171781),
        ("79", 6, 40, true, false, 1524171781)
      )).toDF("station_id", "bikes_available", "docks_available", "is_renting", "is_returning", "last_updated")
      val informationDF = spark.createDataset(Array(
        ("72", "W 52 St & 11 Ave", 40.76727216, -73.99392888),
        ("79", "Franklin St & W B...", 40.71911552, -74.00666661)
      )).toDF("station_id", "name", "latitude", "longitude")

      When("Transformations are applied")
      val resultDF = transformWithTimeWindow(spark, statusDF, informationDF)

      Then("Useful columns are extracted")
      resultDF.show(false)

    }

  }
}
