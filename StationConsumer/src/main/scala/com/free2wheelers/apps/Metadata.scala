package com.free2wheelers.apps

case class Metadata(
                     ingestion_time: Double,
                     producer_id: String,
                     message_uuid: String,
                     size: Double
                   )