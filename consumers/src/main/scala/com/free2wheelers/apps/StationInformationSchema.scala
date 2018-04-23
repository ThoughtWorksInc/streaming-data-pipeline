package com.free2wheelers.apps

import org.apache.spark.sql.types._

object StationInformationSchema {
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
                      StructField("name", StringType, true),
                      StructField("short_name", StringType, true),
                      StructField("lat", LongType, true),
                      StructField("lon", LongType, true),
                      StructField("num_docks_disabled", IntegerType, true),
                      StructField("region_id", IntegerType, true),
                      StructField("rental_methods", ArrayType(StringType), true),
                      StructField("capacity", IntegerType, true),
                      StructField("rental_url", StringType, true),
                      StructField("eightd_has_key_dispenser",
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
