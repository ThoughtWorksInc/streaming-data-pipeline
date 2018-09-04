package com.free2wheelers.apps

import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{explode, udf}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.parsing.json.JSON

object StationInformationTransformation {

  case class StationInformation(
                                 station_id: String,
                                 name: String,
                                 latitude: Double,
                                 longitude: Double,
                                 last_updated: Long
                               )

  val toStationInformation: String => Seq[StationInformation] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val metadata = json.get.asInstanceOf[Map[String, Any]]("metadata").asInstanceOf[Map[String, String]]
    val producerId = metadata("producer_id")

    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    producerId match {
      case "producer_station_information" => extractNycStationInformation(payload)
      case "producer_station-san_francisco" => extractSFStationInformation(payload)
    }
  }

  private def extractNycStationInformation(payload: Any) = {
    val data = payload.asInstanceOf[Map[String, Any]]("data")
    val lastUpdated = payload.asInstanceOf[Map[String, Any]]("last_updated").asInstanceOf[Double].toLong
    val stations: Any = data.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationInformation(
          x("station_id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("lat").asInstanceOf[Double],
          x("lon").asInstanceOf[Double],
          lastUpdated
        )
      })
  }

  private def extractSFStationInformation(payload: Any): Seq[StationInformation] = {
    val network = payload.asInstanceOf[Map[String, Any]]("network")

    val stations = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationInformation(
          x("id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("latitude").asInstanceOf[Double],
          x("longitude").asInstanceOf[Double],
          Instant.from(DateTimeFormatter.ISO_INSTANT.parse(x("timestamp").asInstanceOf[String])).getEpochSecond
        )
      })
  }

  def stationInformationJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStationFn: UserDefinedFunction = udf(toStationInformation)

    import spark.implicits._
    jsonDF.select(explode(toStationFn(jsonDF("raw_payload"))) as "station")
      .select($"station.*")
  }

}
