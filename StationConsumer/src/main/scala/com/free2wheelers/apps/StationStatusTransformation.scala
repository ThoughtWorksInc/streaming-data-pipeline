package com.free2wheelers.apps

import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.parsing.json.JSON

object StationStatusTransformation {

  val sfToStationStatus: String => Seq[StationStatus] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractSFStationStatus(payload)
  }

  val nycOnlyToStationStatus: String => Seq[StationStatus] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractNycStationStatus(payload, true)
  }

  val nycToStationStatus: String => Seq[StationStatus] = raw_payload => {
    val payload = JSON.parseFull(raw_payload)
    extractNycStationStatus(payload)
  }

  private def extractNycStationStatus(payload: Any, skipInfo: Boolean = false) = {
    val data = payload.asInstanceOf[Map[String, Any]]("data")

    val lastUpdated = payload.asInstanceOf[Map[String, Any]]("last_updated").asInstanceOf[Double].toLong

    val stations: Any = data.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationStatus(
          x("num_bikes_available").asInstanceOf[Double].toInt,
          x("num_docks_available").asInstanceOf[Double].toInt,
          x("is_renting").asInstanceOf[Double] == 1,
          x("is_returning").asInstanceOf[Double] == 1,
          lastUpdated,
          x("station_id").asInstanceOf[String],

          if (skipInfo) "" else x("name").asInstanceOf[String],
          if (skipInfo) 0D else x("lat").asInstanceOf[Double],
          if (skipInfo) 0D else x("lon").asInstanceOf[Double]
        )
      })
  }

  private def extractSFStationStatus(payload: Any) = {

    val network: Any = payload.asInstanceOf[Map[String, Any]]("network")

    val stations: Any = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationStatus(
          x("free_bikes").asInstanceOf[Double].toInt,
          x("empty_slots").asInstanceOf[Double].toInt,
          x("extra").asInstanceOf[Map[String, Any]]("renting").asInstanceOf[Double] == 1,
          x("extra").asInstanceOf[Map[String, Any]]("returning").asInstanceOf[Double] == 1,
          Instant.from(DateTimeFormatter.ISO_INSTANT.parse(x("timestamp").asInstanceOf[String])).getEpochSecond,
          x("id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("latitude").asInstanceOf[Double],
          x("longitude").asInstanceOf[Double]
        )
      })
  }

  def sfStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(sfToStationStatus)

    import spark.implicits._

    jsonDF.select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }

  def nycOnlyStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(nycOnlyToStationStatus)

    import spark.implicits._

    jsonDF.select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }

  def nycStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(nycToStationStatus)

    import spark.implicits._

    jsonDF.select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }
}
