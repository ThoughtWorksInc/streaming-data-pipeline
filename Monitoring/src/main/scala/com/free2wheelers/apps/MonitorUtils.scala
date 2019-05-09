package com.free2wheelers.apps

import org.apache.spark.sql.DataFrame

object MonitorUtils {

  def isLongitudeOrLatitudeNull(csvDF: DataFrame): Boolean = csvDF.where(csvDF.col("latitude").isNull || csvDF.col("longitude").isNull).count() != 0L

  def isStationIdUnique(csvDF: DataFrame) = (csvDF.select("station_id").distinct().count()) != csvDF.count()

}
