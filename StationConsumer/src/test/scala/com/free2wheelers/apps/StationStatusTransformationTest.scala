package com.free2wheelers.apps

import com.free2wheelers.apps.StationStatusTransformation.{informationJson2DF, statusJson2DF}
import org.apache.spark.sql.SparkSession
import org.scalatest._

class StationStatusTransformationTest extends FeatureSpec with Matchers with GivenWhenThen {
  feature("Apply transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

    scenario("Transform station_status data frame and extract useful fields") {

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
      val resultDF1 = statusJson2DF(testDF1)

      Then("Useful columns are extracted")
      resultDF1.schema.fields(0).name should be("station_id")
      resultDF1.schema.fields(0).dataType.typeName should be("string")
      resultDF1.schema.fields(1).name should be("bikes_available")
      resultDF1.schema.fields(1).dataType.typeName should be("integer")
      resultDF1.schema.fields(2).name should be("docks_available")
      resultDF1.schema.fields(2).dataType.typeName should be("integer")
      resultDF1.schema.fields(3).name should be("is_renting")
      resultDF1.schema.fields(3).dataType.typeName should be("boolean")
      resultDF1.schema.fields(4).name should be("is_returning")
      resultDF1.schema.fields(4).dataType.typeName should be("boolean")
      resultDF1.schema.fields(5).name should be("last_updated")
      resultDF1.schema.fields(5).dataType.typeName should be("long")

      val row1 = resultDF1.where("station_id = 72").head()
      row1.get(0) should be("72")
      row1.get(1) should be(1)
      row1.get(2) should be(33)
      row1.get(3) shouldBe false
      row1.get(4) shouldBe true
      row1.get(5) should be(1524170881)

      val row2 = resultDF1.where("station_id = 73").head()
      row2.get(0) should be("73")
      row2.get(1) should be(3)
      row2.get(2) should be(50)
      row2.get(3) shouldBe true
      row2.get(4) shouldBe false
      row2.get(5) should be(1524170881)
    }

    scenario("Transform station_information data frame and extract useful fields") {

      val testStationInformationData =
        """{
        "metadata": {
          "producer_id": "producer_station_information",
          "size": 1323,
          "message_id": "1234-3224-2444242-fm2kf23",
          "ingestion_time": 1524493544235
        },
        "payload": {
            "last_updated":1524600463,
            "ttl":10,
            "data":{
              "stations":[
              {
                "station_id":"72",
                "name":"W 52 St & 11 Ave",
                "short_name":"6926.01",
                "lat":40.76727216,
                "lon":-73.99392888,
                "region_id":71,
                "rental_methods":[
                  "KEY",
                  "CREDITCARD"
                ],
                "capacity":39,
                "rental_url":"http://app.citibikenyc.com/S6Lr/IBV092JufD?station_id=72",
                "eightd_has_key_dispenser":false
              },
              {
                "station_id":"79",
                "name":"Franklin St & W Broadway",
                "short_name":"5430.08",
                "lat":40.71911552,
                "lon":-74.00666661,
                "region_id":71,
                "rental_methods":[
                  "KEY",
                  "CREDITCARD"
                ],
                "capacity":33,
                "rental_url":"http://app.citibikenyc.com/S6Lr/IBV092JufD?station_id=79",
                "eightd_has_key_dispenser":false
              }
            ]
          }
        }
      }"""

      Given("Sample data for station_information")
      val testDF2 = Seq(testStationInformationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF2 = informationJson2DF(testDF2, spark)

      Then("Useful columns are extracted")
      resultDF2.schema.fields(0).name should be("station_id")
      resultDF2.schema.fields(0).dataType.typeName should be("string")
      resultDF2.schema.fields(1).name should be("name")
      resultDF2.schema.fields(1).dataType.typeName should be("string")
      resultDF2.schema.fields(2).name should be("latitude")
      resultDF2.schema.fields(2).dataType.typeName should be("double")
      resultDF2.schema.fields(3).name should be("longitude")
      resultDF2.schema.fields(3).dataType.typeName should be("double")

      val row1 = resultDF2.where("station_id = 72").head()
      row1.get(0) should be("72")
      row1.get(1) should be("W 52 St & 11 Ave")
      row1.get(2) should be(40.76727216)
      row1.get(3) should be(-73.99392888)

      val row2 = resultDF2.where("station_id = 79").head()
      row2.get(0) should be("79")
      row2.get(1) should be("Franklin St & W Broadway")
      row2.get(2) should be(40.71911552)
      row2.get(3) should be(-74.00666661)
    }

    scenario("Transform station_information data frame and extract useful fields of SF") {
      val sfStationInformationData =
        """{
        "metadata": {
          "producer_id": "sf_producer_station_information",
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

      Given("Sample data for station_information")
      val testDF2 = Seq(sfStationInformationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF2 = informationJson2DF(testDF2, spark)

      Then("Useful columns are extracted")
      resultDF2.schema.fields(0).name should be("station_id")
      resultDF2.schema.fields(0).dataType.typeName should be("string")
      resultDF2.schema.fields(1).name should be("name")
      resultDF2.schema.fields(1).dataType.typeName should be("string")
      resultDF2.schema.fields(2).name should be("latitude")
      resultDF2.schema.fields(2).dataType.typeName should be("double")
      resultDF2.schema.fields(3).name should be("longitude")
      resultDF2.schema.fields(3).dataType.typeName should be("double")

      val row1 = resultDF2.where("station_id = '0d1cc38593e42fd252223058f5e2a1e3'").head()
      row1.get(0) should be("0d1cc38593e42fd252223058f5e2a1e3")
      row1.get(1) should be("Koshland Park")
      row1.get(2) should be(37.77341396997343)
      row1.get(3) should be(-122.42731690406801)

      val row2 = resultDF2.where("station_id = '744a78dbf1295803e62b64fd7579ddef'").head()
      row2.get(0) should be("744a78dbf1295803e62b64fd7579ddef")
      row2.get(1) should be("47th St at San Pablo Ave")
      row2.get(2) should be(37.83563220458518)
      row2.get(3) should be(-122.28105068206787)
    }
  }
}
