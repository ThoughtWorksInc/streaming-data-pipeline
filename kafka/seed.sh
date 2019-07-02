#!/bin/bash

set -e

environment_type=$1

if [[ "${environment_type}" == "uat" ]]
then
    topic_retention_config = "--config retention.bytes=94371840 --config retention.ms=86400000"
else
    topic_retention_config = ""
fi


topic_config="--replication-factor 1 --partitions 1 --config cleanup.policy=delete ${topic_retention_config}"



kafka-configs --bootstrap-server localhost:9092 --alter --add-config retention.ms=3600000,cleanup.policy=delete --entity-type brokers --entity-name 0


kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_information

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_marseille

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_nyc

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_nyc_v2

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_sf

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_status

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_data_test

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_information

kafka-topics --zookeeper localhost:2181 --create --if-not-exists ${topic_config} --topic station_status
