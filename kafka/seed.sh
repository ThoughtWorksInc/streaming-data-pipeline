#!/bin/bash

set -e

environment_type=$1

kafka-configs --bootstrap-server localhost:9092 --alter --add-config retention.ms=3600000,cleanup.policy=delete --entity-type brokers --entity-name 0

topics="station_data_information station_data_marseille station_data_nyc station_data_nyc_v2 station_data_sf station_data_status station_data_test station_information station_status"

for topic in ${topics}
do
    kafka-topics --zookeeper localhost:2181 --create --if-not-exists --replication-factor 1 --partitions 1 --config cleanup.policy=delete --topic ${topic}

    if [[ "${environment_type}" == "uat" ]]
    then
        kafka-configs --zookeeper localhost:2181 --alter --add-config retention.bytes=94371840,retention.ms=86400000 --entity-type topics --entity-name ${topic}
    fi
done
