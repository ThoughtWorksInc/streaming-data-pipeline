package com.free2wheelers.apps

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
        ,col("station.num_bikes_available") + col("station.num_ebikes_available") as "bikes_available"
        ,col("station.num_docks_available") as "docks_available"
        ,col("station.is_renting") === 1 as "is_renting"
        ,col("station.is_returning") === 1 as "is_returning"
        ,col("last_updated"))
  }

  case class Station(
                      station_id: String,
                      name: String,
                      latitude: Double,
                      longitude: Double
                     )

  val toStation: String => Seq[Station] = (raw_payload) => {
    val json = JSON.parseFull(raw_payload)
    val metadata = json.get.asInstanceOf[Map[String, Any]]("metadata").asInstanceOf[Map[String, String]]
    val producerId = metadata("producer_id")

    val payload = json.get.asInstanceOf[Map[String, Any]]("payload")
    producerId match {
      case "producer_station_information" => extractNycStation(payload)
      case "sf_producer_station_information" => extractSFStation(payload)
    }
  }

  private def extractNycStation(payload: Any) = {
    val data = payload.asInstanceOf[Map[String, Any]]("data")

    val stations: Any = data.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        Station(
          x("station_id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("lat").asInstanceOf[Double],
          x("lon").asInstanceOf[Double]
        )
      })
  }

  private def extractSFStation(payload: Any) = {
    val network = payload.asInstanceOf[Map[String, Any]]("network")

    val stations = network.asInstanceOf[Map[String, Any]]("stations")

    stations.asInstanceOf[Seq[Map[String, Any]]]
      .map(x => {
        Station(
          x("id").asInstanceOf[String],
          x("name").asInstanceOf[String],
          x("latitude").asInstanceOf[Double],
          x("longitude").asInstanceOf[Double]
        )
      })
  }

  def informationJson2DF(jsonDF: DataFrame, spark: SparkSession): DataFrame = {
    val toStationFn: UserDefinedFunction = udf(toStation)

    import spark.implicits._
    val frame = jsonDF
        .select(explode(toStationFn(jsonDF("raw_payload"))) as "station")
        .select($"station.*")

    return frame
  }
}
