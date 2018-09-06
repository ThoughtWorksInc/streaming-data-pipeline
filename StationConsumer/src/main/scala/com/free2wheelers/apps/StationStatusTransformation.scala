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
    val payload = JSON.parseFull(raw_payload).get
    extractNycStationStatus(payload)
  }

  private def extractNycStationStatus(payload: Any, skipInfo: Boolean = false) = {
    val data = payload.asInstanceOf[Map[String, Any]]

    val lastUpdated = payload.asInstanceOf[Map[String, Any]]("last_updated").asInstanceOf[Double].toLong

    //    val stations: Any = data.asInstanceOf[Map[String, Any]]("stations")

    //    stations.asInstanceOf[Seq[Map[String, Any]]]

    val status = StationStatus(
      data("bikes_available").asInstanceOf[Double].toInt,
      data("docks_available").asInstanceOf[Double].toInt,
      data("is_renting").asInstanceOf[Boolean],
      data("is_returning").asInstanceOf[Boolean],
      lastUpdated,
      data("station_id").asInstanceOf[String],

      if (skipInfo) "" else data("name").asInstanceOf[String],
      if (skipInfo) 0D else data("latitude").asInstanceOf[Double],
      if (skipInfo) 0D else data("longitude").asInstanceOf[Double]
    )
      //      })
      Array(status)
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
