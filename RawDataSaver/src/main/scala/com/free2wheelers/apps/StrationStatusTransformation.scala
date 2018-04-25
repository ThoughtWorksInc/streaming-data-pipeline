package com.free2wheelers.apps

import com.free2wheelers.apps.StationApp.spark
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions.{explode, from_json}
import org.apache.spark.sql.types.StructType

/**
  * Created by dholness on 4/20/18.
  */
object StrationStatusTransformation {
  import spark.implicits._

  def denormalizeStations(stationStatusDF: DataFrame) = {
    stationStatusDF
      .withColumn("stations",explode($"payload.data.stations"))
  }

  def denormalizeStream(stationStatusDF: DataFrame):DataFrame={
    denormalizeStations(stationStatusDF
      .select(
        $"metadata.producer_id" as "producer_id"
        ,$"metadata.message_id" as "message_id"
        ,$"metadata.ingestion_time" as "ingestion_time"
        ,$"payload.last_updated" as "last_updated"
        ,$"payload.ttl" as "ttl"
        ,$"payload.data.stations" as "stations")
    )
  }

  def json2DF(schema:StructType)(jsonDF:DataFrame):DataFrame={
    jsonDF
      .selectExpr("CAST(value AS STRING) as raw_payload")
      .select(from_json($"raw_payload", schema)
        .as("json_payload"))
      .select("json_payload.*")
  }

}
