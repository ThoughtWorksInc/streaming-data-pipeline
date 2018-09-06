package com.free2wheelers.apps

import org.apache.spark.sql.types._

object StationStatusSchema {

  val schema = StructType(
      List(
        StructField(
          "metadata",
          StructType(List(
            StructField("producer_id", StringType, false),
            StructField("size", IntegerType, false),
            StructField("message_id", StringType, false),
            StructField("ingestion_time", LongType, false)
          )),
          false
        ),
        StructField(
          "payload",
          StructType(List(
            StructField("last_updated", LongType, false),
            StructField("ttl", IntegerType, false),
            StructField(
              "data",
              StructType(List(
                StructField(
                  "stations",
                  ArrayType(
                    StructType(List(
                      StructField("station_id", StringType, false),
                      StructField("num_bikes_available", IntegerType, false),
                      StructField("num_ebikes_available", IntegerType, false),
                      StructField("num_bikes_disabled", IntegerType, true),
                      StructField("num_docks_available", IntegerType, false),
                      StructField("num_docks_disabled", IntegerType, true),
                      StructField("is_installed", IntegerType, false),
                      StructField("is_renting", IntegerType, false),
                      StructField("is_returning", IntegerType, false),
                      StructField("last_reported", LongType, false),
                      StructField("eightd_has_available_keys", BooleanType, false)
                    ))
                  ),
                  false
                )
              )),
              false
            )
          )),false
        )
      ))

}
