package com.free2wheelers.apps

import org.apache.spark.sql.functions.{col, count}
import org.apache.spark.sql.{AnalysisException, DataFrame, SparkSession}

class StationReportValidator(spark: SparkSession) {

    val LAT_LONG_REGEX = "^-?\\d+\\.\\d+$"

    val DUPLICATION_THRESHOLD = 1

    def isValid(stationMartDF: DataFrame): Boolean = {
        try {
            val duplicatedStationCount = check_duplicated_stations(stationMartDF)
            val numberOfInvalidLatitudeStations = check_invalid_latitude(stationMartDF)
            val numberOfInvalidLongitudeStations = check_invalid_longitude(stationMartDF)
            val isEmptyDF = stationMartDF.rdd.isEmpty()

            val isValid = (!isEmptyDF) &&
                duplicatedStationCount == 0 &&
                numberOfInvalidLongitudeStations == 0 &&
                numberOfInvalidLatitudeStations == 0

            isValid
        } catch {
            case analysis_exception: AnalysisException => {
                analysis_exception.printStackTrace()
                throw new RuntimeException("Invalid file")
            }
        }
    }

    private def check_invalid_longitude(df: DataFrame): Int = {
        import spark.implicits._

        val foundValidLongitudeDF = df.filter($"longitude".rlike(LAT_LONG_REGEX))
        df.count().toInt - foundValidLongitudeDF.count().toInt
    }

    private def check_invalid_latitude(df: DataFrame): Int = {
        import spark.implicits._
        val foundValidLatitudeDF = df.filter($"latitude".rlike(LAT_LONG_REGEX))

        df.count.toInt - foundValidLatitudeDF.count.toInt
    }

    private def check_duplicated_stations(stationMartDF: DataFrame): Int = {
        stationMartDF
            .groupBy("station_id")
            .agg(count("station_id") as "total")
            .filter(col("total") > DUPLICATION_THRESHOLD)
            .count().intValue()
    }

}
