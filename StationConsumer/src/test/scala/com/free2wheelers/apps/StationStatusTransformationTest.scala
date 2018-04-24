package com.free2wheelers.apps

import org.scalatest._
import org.apache.spark.sql.SparkSession
import com.free2wheelers.apps.StationStatusTransformation.json2DF

class StationStatusTransformationTest extends FeatureSpec with Matchers with GivenWhenThen{
  feature("Apply transformations to data frame") {
    scenario("Transform data frame and extract useful fields") {

      val testStationStatusData =
        """{
        "metadata": {
          "producer_id": "producer_station_status",
          "size": 1323,
          "message_id": "1234-3224-2444242-fm2kf23",
          "ingestion_time": 1524493544235
        },
        "payload": {
          "last_updated": 1524170881,
          "ttl": 10,
          "data": {
            "stations": [
              {
                "station_id": "72",
                "num_bikes_available": 1,
                "num_ebikes_available": 0,
                "num_bikes_disabled": 3,
                "num_docks_available": 33,
                "num_docks_disabled": 0,
                "is_installed": 1,
                "is_renting": 0,
                "is_returning": 1,
                "last_reported": 1524170299,
                "eightd_has_available_keys": false
              },
              {
                "station_id": "73",
                "num_bikes_available": 2,
                "num_ebikes_available": 1,
                "num_bikes_disabled": 5,
                "num_docks_available": 50,
                "num_docks_disabled": 0,
                "is_installed": 1,
                "is_renting": 1,
                "is_returning": 0,
                "last_reported": 1524170299,
                "eightd_has_available_keys": false
              }
            ]
          }
        }
      }"""

      Given("Sample data for station_status")
      val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
      import spark.implicits._
      val testDF = Seq(testStationStatusData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF = json2DF(testDF)

      Then("Useful columns are extracted")
      resultDF.schema.fields(0).name should be("station_id")
      resultDF.schema.fields(0).dataType.typeName should be("string")
      resultDF.schema.fields(1).name should be("bikes_available")
      resultDF.schema.fields(1).dataType.typeName should be("integer")
      resultDF.schema.fields(2).name should be("docks_available")
      resultDF.schema.fields(2).dataType.typeName should be("integer")
      resultDF.schema.fields(3).name should be("is_renting")
      resultDF.schema.fields(3).dataType.typeName should be("boolean")
      resultDF.schema.fields(4).name should be("is_returning")
      resultDF.schema.fields(4).dataType.typeName should be("boolean")
      resultDF.schema.fields(5).name should be("last_updated")
      resultDF.schema.fields(5).dataType.typeName should be("long")

      val row1 = resultDF.where("station_id = 72").head()
      row1.get(0) should be("72")
      row1.get(1) should be(1)
      row1.get(2) should be(33)
      row1.get(3) shouldBe false
      row1.get(4) shouldBe true
      row1.get(5) should be(1524170881)

      val row2 = resultDF.where("station_id = 73").head()
      row2.get(0) should be("73")
      row2.get(1) should be(3)
      row2.get(2) should be(50)
      row2.get(3) shouldBe true
      row2.get(4) shouldBe false
      row2.get(5) should be(1524170881)
    }
  }
}
