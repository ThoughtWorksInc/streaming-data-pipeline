package com.free2wheelers.apps

import com.free2wheelers.apps.StationStatusTransformation._
import org.apache.spark.sql.SparkSession
import org.scalatest._

class StationStatusTransformationTest extends FeatureSpec with Matchers with GivenWhenThen {

  feature("Apply station status transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

    scenario("Transform station_status data frame and extract useful fields of NYC") {

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
      val testDF1 = Seq(testStationStatusData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF1 = stationStatusJson2DF(testDF1, spark)

      Then("Useful columns are extracted")
      resultDF1.schema.fields(0).name should be("bikes_available")
      resultDF1.schema.fields(0).dataType.typeName should be("integer")
      resultDF1.schema.fields(1).name should be("docks_available")
      resultDF1.schema.fields(1).dataType.typeName should be("integer")
      resultDF1.schema.fields(2).name should be("is_renting")
      resultDF1.schema.fields(2).dataType.typeName should be("boolean")
      resultDF1.schema.fields(3).name should be("is_returning")
      resultDF1.schema.fields(3).dataType.typeName should be("boolean")
      resultDF1.schema.fields(4).name should be("last_updated")
      resultDF1.schema.fields(4).dataType.typeName should be("long")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")

      val row1 = resultDF1.where("station_id = 72").head()
      row1.get(0) should be(1)
      row1.get(1) should be(33)
      row1.get(2) shouldBe false
      row1.get(3) shouldBe true
      row1.get(4) should be(1524170881)
      row1.get(5) should be("72")

      val row2 = resultDF1.where("station_id = 73").head()
      row2.get(0) should be(2)
      row2.get(1) should be(50)
      row2.get(2) shouldBe true
      row2.get(3) shouldBe false
      row2.get(4) should be(1524170881)
      row2.get(5) should be("73")
    }

    scenario("Transform status information data frame and extract useful fields of SF") {
      val sfInformationData =
        """{
        "metadata": {
          "producer_id": "producer_station-san_francisco",
          "size": 1323,
          "message_id": "1234-3224-2444242-fm2kf23",
          "ingestion_time": 1524493544235
        },
        "payload": {
          "network": {
            "company": [
              "Motivate International, Inc."
            ],
            "gbfs_href": "https://gbfs.fordgobike.com/gbfs/gbfs.json",
            "href": "/v2/networks/ford-gobike",
            "id": "ford-gobike",
            "location": {
              "city": "San Francisco Bay Area, CA",
              "country": "US",
              "latitude": 37.7141454,
              "longitude": -122.25
            },
            "name": "Ford GoBike",
            "stations": [
              {
                "empty_slots": 26,
                "extra": {
                  "address": null,
                  "last_updated": 1535550884,
                  "renting": 1,
                  "returning": 1,
                  "uid": "56"
                },
                "free_bikes": 1,
                "id": "0d1cc38593e42fd252223058f5e2a1e3",
                "latitude": 37.77341396997343,
                "longitude": -122.42731690406801,
                "name": "Koshland Park",
                "timestamp": "2018-08-29T13:58:57.031000Z"
              },
              {
                "empty_slots": 9,
                "extra": {
                  "address": null,
                  "last_updated": 1535550804,
                  "renting": 1,
                  "returning": 1,
                  "uid": "152"
                },
                "free_bikes": 10,
                "id": "744a78dbf1295803e62b64fd7579ddef",
                "latitude": 37.83563220458518,
                "longitude": -122.28105068206787,
                "name": "47th St at San Pablo Ave",
                "timestamp": "2018-08-29T13:58:57.184000Z"
              }
            ]
          }
        }
      }"""


      Given("Sample data for status_information")
      val testDF2 = Seq(sfInformationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF2 = stationStatusJson2DF(testDF2, spark)

      Then("Useful columns are extracted")
      resultDF2.schema.fields(0).name should be("bikes_available")
      resultDF2.schema.fields(0).dataType.typeName should be("integer")
      resultDF2.schema.fields(1).name should be("docks_available")
      resultDF2.schema.fields(1).dataType.typeName should be("integer")
      resultDF2.schema.fields(2).name should be("is_renting")
      resultDF2.schema.fields(2).dataType.typeName should be("boolean")
      resultDF2.schema.fields(3).name should be("is_returning")
      resultDF2.schema.fields(3).dataType.typeName should be("boolean")
      resultDF2.schema.fields(4).name should be("last_updated")
      resultDF2.schema.fields(4).dataType.typeName should be("long")
      resultDF2.schema.fields(5).name should be("station_id")
      resultDF2.schema.fields(5).dataType.typeName should be("string")

      val row1 = resultDF2.where("station_id = '0d1cc38593e42fd252223058f5e2a1e3'").head()
      row1.get(0) should be(1)
      row1.get(1) should be(26)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe true
      row1.get(4) should be(1535551137)

      val row2 = resultDF2.where("station_id = '744a78dbf1295803e62b64fd7579ddef'").head()
      row2.get(0) should be(10)
      row2.get(1) should be(9)
      row2.get(2) shouldBe true
      row2.get(3) shouldBe true
      row2.get(4) should be(1535551137)

    }

  }
}
