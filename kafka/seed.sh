#!/bin/bash

set -e

kafka-configs --bootstrap-server localhost:9092 --alter --add-config retention.ms=3600000,cleanup.policy=delete --entity-type brokers --entity-name 0


kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_information

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_marseille

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_nyc

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_nyc_v2

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_sf

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_status

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_data_test
kafka-configs --zookeeper localhost:2181 --alter --add-config retention.ms=1800000,cleanup.policy=delete --entity-type topics --entity-name station_data_test

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_information

kafka-topics --zookeeper localhost:2181 --create --replication-factor 1 --partitions 1 --if-not-exists --topic station_status
