package com.free2wheelers.apps

import com.free2wheelers.apps.StationApp.unionStationData
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.StringType
import org.scalatest.{FeatureSpec, Matchers}

class StationAppTest extends FeatureSpec with Matchers {

  feature("Apply station status transformations to data frame") {
    val spark = SparkSession.builder.appName("Test App").master("local").getOrCreate()

    import spark.implicits._

    scenario("Should return timestamp string in last_updated column") {
      val df = List(StationStatus(4, 5, true, true, "2018-11-08T17:43:48.632000Z", "123", "Best SF Bikes", 0, 0)).toDF()

      val result = unionStationData(df, df, spark)

      result.schema.fields(4).name should be("last_updated")
      result.schema.fields(4).dataType should be(StringType)

      result.head().last_updated should be("2018-11-08T17:43:48.632000Z")
    }

    scenario("Should return only the latest data for given station ID") {
      val sampleDataLatest = StationStatus(4, 5, true, true, "2018-11-08T17:43:48.632000Z", "123", "Best SF Bikes", 0, 0)
      val sampleData = StationStatus(4, 5, true, true, "2018-11-08T13:43:48.632000Z", "123", "Best SF Bikes", 0, 0)
      val df1 = List(sampleDataLatest).toDF()
      val df2 = List(sampleData).toDF()
      val result = unionStationData(df1, df2, spark)

      result.count() should be(1)

      result.head().last_updated should be(sampleDataLatest.last_updated)
    }
  }
}
