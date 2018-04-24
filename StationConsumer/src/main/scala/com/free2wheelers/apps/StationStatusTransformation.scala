package com.free2wheelers.apps

import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._

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

  def informationJson2DF(jsonDF: DataFrame): DataFrame = {
    jsonDF
      .select(from_json(col("raw_payload"), StationInformationSchema.schema).as("station_information"))
      .select(col("station_information.payload.data.stations") as "stations")
      .select(explode(col("stations")) as "station")
      .select(
        col("station.station_id") as "station_id",
        col("station.name") as "name",
        col("station.lat") as "latitude",
        col("station.lon") as "longitude"
      )
  }
}
