package com.free2wheelers.apps

import com.free2wheelers.apps.StationInformationTransformation._
import org.apache.spark.sql.SparkSession
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class StationInformationTransformationTest extends FeatureSpec with Matchers with GivenWhenThen {
  feature("Apply station information transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

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
      val resultDF2 = stationInformationJson2DF(testDF2, spark)

      Then("Useful columns are extracted")
      resultDF2.schema.fields(0).name should be("station_id")
      resultDF2.schema.fields(0).dataType.typeName should be("string")
      resultDF2.schema.fields(1).name should be("name")
      resultDF2.schema.fields(1).dataType.typeName should be("string")
      resultDF2.schema.fields(2).name should be("latitude")
      resultDF2.schema.fields(2).dataType.typeName should be("double")
      resultDF2.schema.fields(3).name should be("longitude")
      resultDF2.schema.fields(3).dataType.typeName should be("double")
      resultDF2.schema.fields(4).name should be("last_updated")
      resultDF2.schema.fields(4).dataType.typeName should be("long")

      val row1 = resultDF2.where("station_id = 72").head()
      row1.get(0) should be("72")
      row1.get(1) should be("W 52 St & 11 Ave")
      row1.get(2) should be(40.76727216)
      row1.get(3) should be(-73.99392888)
      row1.get(4) should be(1524600463)

      val row2 = resultDF2.where("station_id = 79").head()
      row2.get(0) should be("79")
      row2.get(1) should be("Franklin St & W Broadway")
      row2.get(2) should be(40.71911552)
      row2.get(3) should be(-74.00666661)
      row1.get(4) should be(1524600463)
    }

    scenario("Transform station_information data frame and extract useful fields of SF") {
      val sfStationInformationData =
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

      Given("Sample data for station_information")
      val testDF2 = Seq(sfStationInformationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF2 = stationInformationJson2DF(testDF2, spark)

      Then("Useful columns are extracted")
      resultDF2.schema.fields(0).name should be("station_id")
      resultDF2.schema.fields(0).dataType.typeName should be("string")
      resultDF2.schema.fields(1).name should be("name")
      resultDF2.schema.fields(1).dataType.typeName should be("string")
      resultDF2.schema.fields(2).name should be("latitude")
      resultDF2.schema.fields(2).dataType.typeName should be("double")
      resultDF2.schema.fields(3).name should be("longitude")
      resultDF2.schema.fields(3).dataType.typeName should be("double")
      resultDF2.schema.fields(4).name should be("last_updated")
      resultDF2.schema.fields(4).dataType.typeName should be("long")

      val row1 = resultDF2.where("station_id = '0d1cc38593e42fd252223058f5e2a1e3'").head()
      row1.get(0) should be("0d1cc38593e42fd252223058f5e2a1e3")
      row1.get(1) should be("Koshland Park")
      row1.get(2) should be(37.77341396997343)
      row1.get(3) should be(-122.42731690406801)
      row1.get(4) should be(1535551137)

      val row2 = resultDF2.where("station_id = '744a78dbf1295803e62b64fd7579ddef'").head()
      row2.get(0) should be("744a78dbf1295803e62b64fd7579ddef")
      row2.get(1) should be("47th St at San Pablo Ave")
      row2.get(2) should be(37.83563220458518)
      row2.get(3) should be(-122.28105068206787)
      row2.get(4) should be(1535551137)
    }

  }
}
