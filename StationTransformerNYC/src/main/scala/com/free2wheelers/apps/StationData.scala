package com.free2wheelers.apps

import java.sql.Timestamp

case class StationData(station_id: String, bikes_available: Int, docks_available: Int,
                       is_renting: Boolean, is_returning: Boolean, last_updated: Long, timestamp: Timestamp,
                       name: String, latitude: Double, longitude: Double)

