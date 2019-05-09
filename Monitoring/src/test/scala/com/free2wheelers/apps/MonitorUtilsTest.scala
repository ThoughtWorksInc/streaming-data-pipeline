package com.free2wheelers.apps

import org.apache.spark.sql.DataFrame
import org.scalatest.Matchers

class MonitorUtilsTest extends FeatureSpecWithSpark with Matchers {
  import spark.implicits._

  feature("check if latitude or longitude is null") {
    scenario("return true if latitude / longitude is null for any row") {
      val testDF: DataFrame = Seq(("400.71413089","10"), ("430.71413089","20"), (null,"100")).toDF("latitude", "longitude")

      assert(MonitorUtils.isLongitudeOrLatitudeNull(testDF))
    }

    scenario("return false if latitude / longitude is not null for all") {
      val testDF: DataFrame = Seq(("400.71413089","10"), ("430.71413089","20"), ("12","100")).toDF("latitude", "longitude")

      assert(!MonitorUtils.isLongitudeOrLatitudeNull(testDF))
    }
  }

  feature("check if station Ids are unique") {
    scenario("return true if all station ids unique") {
      val testDF: DataFrame = Seq("400.71413089","10","12","12").toDF("station_id")

      assert(MonitorUtils.isStationIdUnique(testDF))
    }

    scenario("return false if duplicate station ids") {
      val testDF: DataFrame = Seq("400.71413089","10","121","12").toDF("station_id")

      assert(!MonitorUtils.isStationIdUnique(testDF))
    }
  }
}
