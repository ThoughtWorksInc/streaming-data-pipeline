package com.free2wheelers.apps

import org.apache.spark.sql.types._

object StationStatusSchema {

  val schema = StructType(
    StructType(
      List(
        StructField(
          "metadata",
          StructType(List(
            StructField("producer_id", StringType, true),
            StructField("size", IntegerType, true),
            StructField("message_id", StringType, true),
            StructField("ingestion_time", LongType, true),
            StructField("ingestion_time", LongType, true)
          )),
          true
        ),
        StructField(
          "payload",
          StructType(List(
            StructField("last_updated", LongType, true),
            StructField("ttl", IntegerType, true),
            StructField(
              "data",
              StructType(List(
                StructField(
                  "stations",
                  ArrayType(
                    StructType(List(
                      StructField("station_id", StringType, true),
                      StructField("num_bikes_available", IntegerType, true),
                      StructField("num_ebikes_available", IntegerType, true),
                      StructField("num_bikes_disabled", IntegerType, true),
                      StructField("num_docks_available", IntegerType, true),
                      StructField("num_docks_disabled", IntegerType, true),
                      StructField("is_installed", IntegerType, true),
                      StructField("is_renting", IntegerType, true),
                      StructField("is_returning", IntegerType, true),
                      StructField("last_reported", IntegerType, true),
                      StructField("eightd_has_available_keys",
                                  BooleanType,
                                  true)
                    ))
                  ),
                  true
                )
              )),
              true
            )
          ))
        )
      )))

}
