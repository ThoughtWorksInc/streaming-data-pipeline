package com.free2wheelers.apps

import com.free2wheelers.apps.StationStatusTransformation.{nycStationStatusJson2DF, sfStationStatusJson2DF}
import org.apache.spark.sql.SparkSession
import org.scalatest._

class StationStatusTransformationTest extends FeatureSpec with Matchers with GivenWhenThen {

  feature("Apply station status transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()
    import spark.implicits._

    scenario("Transform nyc station data frame") {

      val testStationData =
        """{
          "station_id":"83",
          "bikes_available":19,
          "docks_available":41,
          "is_renting":true,
          "is_returning":true,
          "last_updated":1536242527,
          "name":"Atlantic Ave & Fort Greene Pl",
          "latitude":40.68382604,
          "longitude":-73.97632328
          }"""

      Given("Sample data for station_status")
      val testDF1 = Seq(testStationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF1 = testDF1.transform(nycStationStatusJson2DF(_, spark))

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
      resultDF1.schema.fields(4).dataType.typeName should be("string")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")
      resultDF1.schema.fields(6).name should be("name")
      resultDF1.schema.fields(6).dataType.typeName should be("string")
      resultDF1.schema.fields(7).name should be("latitude")
      resultDF1.schema.fields(7).dataType.typeName should be("double")
      resultDF1.schema.fields(8).name should be("longitude")
      resultDF1.schema.fields(8).dataType.typeName should be("double")

      val row1 = resultDF1.head()
      row1.get(0) should be(19)
      row1.get(1) should be(41)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe true
      row1.get(4) should be("2018-09-06T14:02:07Z")
      row1.get(5) should be("83")
      row1.get(6) should be("Atlantic Ave & Fort Greene Pl")
      row1.get(7) should be(40.68382604)
      row1.get(8) should be(-73.97632328)
    }


    scenario("Transform SF station data frame") {
      val testSanFranciscoStationData =
        """{
            "metadata": {},
            "payload": {
              "network": {
                "stations": [{
                  "empty_slots":13,
                  "extra":{
                    "address":null,
                    "last_updated":1541698833,
                    "renting":1,
                    "returning":1,
                    "uid":"216"
                  },
                  "free_bikes":5,
                  "id":"c8131aed6f3df2f78149eb338df66e66",
                  "latitude":37.8178269,
                  "longitude":-122.2756976,
                  "name":"San Pablo Ave at 27th St",
                  "timestamp":"2018-11-08T17:43:48.632000Z"
                }]
              }
            }
          }"""


      Given("Sample data for station_status")
      val testDF1 = Seq(testSanFranciscoStationData).toDF("raw_payload")

      When("Transformations are applied")
      val resultDF1 = testDF1.transform(sfStationStatusJson2DF(_, spark))

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
      resultDF1.schema.fields(4).dataType.typeName should be("string")
      resultDF1.schema.fields(5).name should be("station_id")
      resultDF1.schema.fields(5).dataType.typeName should be("string")
      resultDF1.schema.fields(6).name should be("name")
      resultDF1.schema.fields(6).dataType.typeName should be("string")
      resultDF1.schema.fields(7).name should be("latitude")
      resultDF1.schema.fields(7).dataType.typeName should be("double")
      resultDF1.schema.fields(8).name should be("longitude")
      resultDF1.schema.fields(8).dataType.typeName should be("double")

      val row1 = resultDF1.head()
      row1.get(0) should be(5)
      row1.get(1) should be(13)
      row1.get(2) shouldBe true
      row1.get(3) shouldBe true
      row1.get(4) should be("2018-11-08T17:43:48.632000Z")
      row1.get(5) should be("c8131aed6f3df2f78149eb338df66e66")
      row1.get(6) should be("San Pablo Ave at 27th St")
      row1.get(7) should be(37.8178269)
      row1.get(8) should be(-122.2756976)
    }
  }
}
