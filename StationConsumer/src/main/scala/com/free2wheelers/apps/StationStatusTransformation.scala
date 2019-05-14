package com.free2wheelers.apps

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{Column, DataFrame, SparkSession}

import scala.util.parsing.json.JSON

object StationStatusTransformation {

  val sfToStationStatus: (String, Timestamp) => Seq[StationStatus] = (raw_payload, consumption_time) => {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractSFStationStatus(payload, consumption_time)
  }

  val franceToStationStatus:  (String, Timestamp) => Seq[StationStatus]  =  (raw_payload, consumption_time)=> {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractFranceStationStatus(payload, consumption_time)
  }

  private def extractSFStationStatus(payload: Any, consumptionTime: Timestamp) = {
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
          x("longitude").asInstanceOf[Double],
          consumptionTime
        )
      })
  }

  private def extractFranceStationStatus(payload: Any, consumptionTime: Timestamp) = {
    val network: Any = payload.asInstanceOf[Map[String, Any]]("network")
    val stations: Any = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        StationStatus(
          x("free_bikes").asInstanceOf[Double].toInt,
          x("empty_slots").asInstanceOf[Double].toInt,
          is_renting = true,
          is_returning = true,
          Instant.from(DateTimeFormatter.ISO_INSTANT.parse(x("timestamp").asInstanceOf[String])).getEpochSecond,
          x("id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("latitude").asInstanceOf[Double],
          x("longitude").asInstanceOf[Double],
          consumptionTime
        )
      })
  }


  def sfStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: Column): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(sfToStationStatus)
    import spark.implicits._

    val ctJsonDF = jsonDF
      .withColumn("consumption_time", consumptionTime)

    ctJsonDF
      .select(explode(toStatusFn(ctJsonDF("raw_payload"), ctJsonDF("consumption_time"))) as "status")
      .select($"status.*")
  }

  def franceStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: Column): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(franceToStationStatus)

    import spark.implicits._

    val ctJsonDF = jsonDF
      .withColumn("consumption_time", consumptionTime)

    ctJsonDF.select(explode(toStatusFn(ctJsonDF("raw_payload"), ctJsonDF("consumption_time"))) as "status")
      .select($"status.*")

  }

  def nycStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: Column): DataFrame = {
    import spark.implicits._

    jsonDF
      .withColumn("consumption_time", consumptionTime)
      .select(from_json($"raw_payload", ScalaReflection.schemaFor[StationStatus].dataType) as "status")
      .select($"status.*")
  }
}
