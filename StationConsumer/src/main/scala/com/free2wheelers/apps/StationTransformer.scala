package com.free2wheelers.apps

import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{DataFrame, SparkSession}

import scala.util.parsing.json.JSON

object StationTransformer {

  val epochSecondToDatetimeString: Long => String = epochSecond => {
    DateTimeFormatter.ISO_INSTANT.format(Instant.ofEpochSecond(epochSecond))
  }

  private val transformerMap = Map(
    Cities.Newyork -> { raw_payload: String => applyTransformation(raw_payload, extractSFStationStatus) },
    Cities.SanFrancisco -> { raw_payload: String => applyTransformation(raw_payload, extractSFStationStatus) },
    Cities.Marseille -> { raw_payload: String => applyTransformation(raw_payload, extractMarseilleStationStatus) }
  )

  def transformFromJson2DF(jsonDF: DataFrame, spark: SparkSession, city: Cities.Value): DataFrame = {
    statusJson2DF(jsonDF, udf(transformerMap(city)), spark)
  }

  private def applyTransformation(raw_payload: String, extractionFunction: Any => Seq[StationStatus]) = {

    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractionFunction(payload)
  }

  private def extractSFStationStatus(payload: Any) = {

    extractStationStatus(
      payload,
      stationData => stationData("extra").asInstanceOf[Map[String, Any]]("renting").asInstanceOf[Double] == 1,
      stationData => stationData("extra").asInstanceOf[Map[String, Any]]("returning").asInstanceOf[Double] == 1
    )
  }

  private def extractMarseilleStationStatus(payload: Any) = {

    extractStationStatus(
      payload,
      stationData => stationData("extra").asInstanceOf[Map[String, Any]]("bonus").asInstanceOf[Boolean],
      stationData => stationData("extra").asInstanceOf[Map[String, Any]]("banking").asInstanceOf[Boolean]
    )
  }

  private def extractStationStatus(payload: Any, rentingSupplier: Map[String, Any] => Boolean, returningSupplier: Map[String, Any] => Boolean) = {

    val network: Any = payload.asInstanceOf[Map[String, Any]]("network")

    val stations: Any = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationStatus(
          x("free_bikes").asInstanceOf[Double].toInt,
          x("empty_slots").asInstanceOf[Double].toInt,
          rentingSupplier(x),
          returningSupplier(x),
          x("timestamp").asInstanceOf[String],
          x("id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("latitude").asInstanceOf[Double],
          x("longitude").asInstanceOf[Double]
        )
      })
  }

  private def statusJson2DF(jsonDF: DataFrame, toStatusFn: UserDefinedFunction, spark: SparkSession) = {
    import spark.implicits._

    jsonDF.select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }
}
