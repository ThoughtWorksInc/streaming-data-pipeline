package com.free2wheelers.apps

import org.apache.spark.sql.types._

object StationStatusSchemaNew {

  val schema = StructType(
      List(
            StructField("station_id", StringType, false),
            StructField("bikes_available", IntegerType, false),
            StructField("docks_available", StringType, false),
            StructField("is_renting", BooleanType, false),
            StructField("is_returning", BooleanType, false),
            StructField("last_updated", LongType, false),
            StructField("name", StringType, false),
            StructField("latitude", DoubleType, false),
            StructField("longitude", DoubleType, false)
        )
      )

}
