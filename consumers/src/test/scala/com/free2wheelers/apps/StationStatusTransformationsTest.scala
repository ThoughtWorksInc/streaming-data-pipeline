package com.free2wheelers.apps
import com.free2wheelers.apps.StrationStatusTransformation.denormalizeStations
import org.apache.log4j.LogManager
import org.apache.spark.sql.SparkSession
import org.scalatest._
class StationStatusTransformationsTest extends FeatureSpec with GivenWhenThen{
  info("As a user of StationStatusTransformation object")
  info("I want to be able to apply transformations to data frame")
  info("So it can be a denormalized flat dataframe")

  feature("Denormalize streaming dataframe"){
    val log = LogManager.getRootLogger
    val spark =
      SparkSession.builder
        .appName("Test Denormalization")
        .master("local")
        .getOrCreate()
    val stationList=Seq(((
      (
        ("station_id", "72"),
        ("num_bikes_available", 3),
        ("num_ebikes_available", 0),
        ("num_bikes_disabled", 3),
        ("num_docks_available", 33),
        ("num_docks_disabled", 0),
        ("is_installed", 1),
        ("is_renting", 1),
        ("is_returning", 1),
        ("last_reported", 1524170299),
        ("eightd_has_available_keys", false)
      ),
      (
        ("station_id", "73"),
        ("num_bikes_available", 3),
        ("num_ebikes_available", 0),
        ("num_bikes_disabled", 3),
        ("num_docks_available", 33),
        ("num_docks_disabled", 0),
        ("is_installed", 1),
        ("is_renting", 1),
        ("is_returning", 1),
        ("last_reported", 1524170299),
        ("eightd_has_available_keys", false)
      ),
      (
        ("station_id", "74"),
        ("num_bikes_available", 3),
        ("num_ebikes_available", 0),
        ("num_bikes_disabled", 3),
        ("num_docks_available", 33),
        ("num_docks_disabled", 0),
        ("is_installed", 1),
        ("is_renting", 1),
        ("is_returning", 1),
        ("last_reported", 1524170299),
        ("eightd_has_available_keys", false)
      )
    )))

    scenario("Explode stations array to station column"){
      Given("Sample dataframe")

        try {
          import spark.implicits._

          val testDF = stationList.toDF("payload.data.stations")
          log.info("DEBUG 1 ................................")
          When("Transformation is applied")
          val transformedDF = testDF.transform(denormalizeStations)
          log.info("DEBUG 2 ................................")
          assert(testDF.count().equals(1))
          assert(transformedDF.count().equals(3))
          log.info("DEBUG 3 ................................")
        } catch {
          case ex:Throwable =>ex.printStackTrace()
        }
    }
  }
}
