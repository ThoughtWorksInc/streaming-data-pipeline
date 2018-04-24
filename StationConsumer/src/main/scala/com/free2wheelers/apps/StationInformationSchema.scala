package com.free2wheelers.apps

import org.apache.spark.sql.types._

object StationInformationSchema {
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
                    StructField("name", StringType, false),
                    StructField("short_name", StringType, true),
                    StructField("lat", DoubleType, false),
                    StructField("lon", DoubleType, false),
                    StructField("num_docks_disabled", IntegerType, true),
                    StructField("region_id", IntegerType, true),
                    StructField("rental_methods", ArrayType(StringType), true),
                    StructField("capacity", IntegerType, true),
                    StructField("rental_url", StringType, true),
                    StructField("eightd_has_key_dispenser", BooleanType, true)
                  ))
                ),
                false
              )
            )),
            false
          )
        ))
      )
    ))
}
