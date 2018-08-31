package com.free2wheelers.apps

import java.text.SimpleDateFormat
import java.util.TimeZone

import org.scalatest.{FeatureSpec, Matchers}

class SimpleTimeFormatterTest extends FeatureSpec with Matchers {

  feature("should parse time correctly") {

    scenario("there are two different time type") {

      val date = StationStatusTransformation.parseTime("2018-08-31T05:52:09Z")

      assert(date.getTime == 1535694720000L)

      val date1 = StationStatusTransformation.parseTime("2018-08-29T13:58:57.031000Z")

      assert(date1.getTime == 1535551085000L)
    }
  }

}
