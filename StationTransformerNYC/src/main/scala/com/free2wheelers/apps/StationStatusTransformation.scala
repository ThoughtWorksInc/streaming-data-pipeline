package com.free2wheelers.apps

import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.parsing.json.JSON

object StationStatusTransformation {

  def statusJson2DF(jsonDF: DataFrame): DataFrame = {
    jsonDF
      .select(from_json(col("raw_payload"), StationStatusSchema.schema).as("station_status"))
      .select(col("station_status.payload.data.stations") as "stations", col("station_status.payload.last_updated") as "last_updated")
      .select(explode(col("stations")) as "station", col("last_updated"))
      .select(col("station.station_id") as "station_id"
        , col("station.num_bikes_available") + col("station.num_ebikes_available") as "bikes_available"
        , col("station.num_docks_available") as "docks_available"
        , col("station.is_renting") === 1 as "is_renting"
        , col("station.is_returning") === 1 as "is_returning"
        , col("last_updated"))
  }

  case class StationStatus(
                            bikes_available: Integer,
                            docks_available: Integer,
                            is_renting: Boolean,
                            is_returning: Boolean,
                            last_updated: Long,
                            station_id: String
                          )

  val toStationStatus: String => Seq[StationStatus] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val metadata = json.get.asInstanceOf[Map[String, Any]]("metadata").asInstanceOf[Map[String, String]]
    val producerId = metadata("producer_id")

    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    producerId match {
      case "producer_station-san_francisco" => extractSFStationStatus(payload)
      case "producer_station_status" => extractNycStationStatus(payload)
    }
  }

  private def extractNycStationStatus(payload: Any) = {
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
          x("station_id").asInstanceOf[String]
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
          x("id").asInstanceOf[String]
        )
      })
  }

  def stationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(toStationStatus)

    import spark.implicits._

    jsonDF.select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }
}
