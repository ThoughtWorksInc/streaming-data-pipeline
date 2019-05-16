#!/bin/sh

nohup /bin/sleep 5 && /opt/kafka_2.11-0.10.0.1/bin/kafka-topics.sh --create --partitions 6 --replication-factor 3 --zookeeper zookeeper:2181 --topic station_data_sf &
nohup /bin/sleep 5 && /opt/kafka_2.11-0.10.0.1/bin/kafka-topics.sh --create --partitions 6 --replication-factor 3 --zookeeper zookeeper:2181 --topic station_data_france &
nohup /bin/sleep 5 && /opt/kafka_2.11-0.10.0.1/bin/kafka-topics.sh --create --partitions 6 --replication-factor 3 --zookeeper zookeeper:2181 --topic station_information &
start-kafka.sh
