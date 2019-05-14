package com.free2wheelers.apps

import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.{udf, _}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.joda.time.DateTime
import org.apache.spark.sql.functions._

import scala.util.parsing.json.JSON

object StationStatusTransformation {

  val sfToStationStatus: String => Seq[StationStatus] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val metadata = json.get.asInstanceOf[Map[String, Any]]("metadata")
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractSFStationStatus(payload, metadata)
  }

  val franceToStationStatus: String => Seq[StationStatus] = raw_payload => {
    val json = JSON.parseFull(raw_payload)
    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    extractFranceStationStatus(payload)
  }

  val getMetadata: String => Metadata = raw_metadata => {
    val json = JSON.parseFull(raw_metadata)
    val metadata = json.get.asInstanceOf[Map[String, Any]]("metadata")
    extractMetadata(metadata)
  }

  private def extractMetadata(metadata: Any): Metadata = {
    val x = metadata.asInstanceOf[Map[String, Any]]
    Metadata(
      x("ingestion_time").asInstanceOf[Double],
      x("producer_id").toString,
      x("message_id").toString,
      x("size").asInstanceOf[Double]
    )
  }

  private def extractSFStationStatus(payload: Any, metadata: Any) = {

    val network: Any = payload.asInstanceOf[Map[String, Any]]("network")

    val stations: Any = network.asInstanceOf[Map[String, Any]]("stations")

    val ingestionTime = new DateTime(extractMetadata(metadata).ingestion_time.longValue())

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
          x("consumption_time").asInstanceOf[Timestamp]
          //ingestionTime
        )
      })
  }

  private def extractFranceStationStatus(payload: Any) = {

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
          x("consumption_time").asInstanceOf[Timestamp]
        )
      })
  }

  private def getMetadataDF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toMetadataFn: UserDefinedFunction = udf(getMetadata)

    import spark.implicits._

    jsonDF.select(toMetadataFn(jsonDF("raw_payload"))).select($"ingestion_time")
  }


  def sfStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: DateTime): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(sfToStationStatus)
    import spark.implicits._

    jsonDF.printSchema()

    jsonDF
      .withColumn("consumption_time", lit(current_timestamp))
      .select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")
  }

  def franceStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: DateTime): DataFrame = {
    val toStatusFn: UserDefinedFunction = udf(franceToStationStatus)

    import spark.implicits._

    jsonDF.printSchema()

    jsonDF
      .withColumn("consumption_time", lit(current_timestamp))
      .select(explode(toStatusFn(jsonDF("raw_payload"))) as "status")
      .select($"status.*")

  }

  def nycStationStatusJson2DF(jsonDF: DataFrame, spark: SparkSession, consumptionTime: DateTime): DataFrame = {
    import spark.implicits._

    jsonDF.printSchema()

    jsonDF
      .withColumn("consumption_time", lit(current_timestamp))
      .select(from_json($"raw_payload", ScalaReflection.schemaFor[StationStatus].dataType) as "status")
      .select($"status.*")
      //.withColumn("ingestion_time", getMetadataDF(jsonDF, spark).col("ingestion_time"))
  }
}
