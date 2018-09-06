package com.free2wheelers.apps

case class StationStatus(
                          bikes_available: Integer, docks_available: Integer,
                          is_renting: Boolean, is_returning: Boolean,
                          last_updated: Long,
                          station_id: String, name: String,
                          latitude: Double, longitude: Double
                        )