#!/usr/bin/env bash
set -e

cat zookeeper.properties

echo $PATH

PATH=$PATH nohup zookeeper-server-start.sh zookeeper.properties 1> /tmp/zookeeper.log  2>/tmp/zookeeper.error.log &

nohup kafka-server-start.sh kafka.properties 1> /tmp/kafka.log  2>/tmp/kafka.error.log &

start-master.sh -p 7077

start-slave.sh 127.0.0.1:7077

mkdir -p /tmp/spark-events && start-history-server.sh

mkdir -p /tmp/free2wheelers/rawData/stationInformation/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationInformation/data
mkdir -p /tmp/free2wheelers/rawData/stationStatus/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationStatus/data
mkdir -p /tmp/free2wheelers/rawData/stationSanFrancisco/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationSanFrancisco/data
mkdir -p /tmp/free2wheelers/stationMart/checkpoints
mkdir -p /tmp/free2wheelers/stationMart/data



zk_command="zookeeper-shell.sh 127.0.0.1:2181"

$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/stationInformation ''
$zk_command create /free2wheelers/stationInformation/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationInformation/topic station_information
$zk_command create /free2wheelers/stationInformation/checkpointLocation /tmp/free2wheelers/rawData/stationInformation/checkpoints
$zk_command create /free2wheelers/stationInformation/dataLocation /tmp/free2wheelers/rawData/stationInformation/data

$zk_command create /free2wheelers/stationStatus ''
$zk_command create /free2wheelers/stationStatus/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationStatus/topic station_status
$zk_command create /free2wheelers/stationStatus/checkpointLocation /tmp/free2wheelers/rawData/stationStatus/checkpoints
$zk_command create /free2wheelers/stationStatus/dataLocation /tmp/free2wheelers/rawData/stationStatus/data

$zk_command create /free2wheelers/stationDataSF ''
$zk_command create /free2wheelers/stationDataSF/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationDataSF/topic station_data_sf
$zk_command create /free2wheelers/stationDataSF/checkpointLocation /tmp/free2wheelers/rawData/stationDataSF/checkpoints
$zk_command create /free2wheelers/stationDataSF/dataLocation /tmp/free2wheelers/rawData/stationDataSF/data


$zk_command create /free2wheelers/output ''
$zk_command create /free2wheelers/output/checkpointLocation /tmp/free2wheelers/stationMart/checkpoints
$zk_command create /free2wheelers/output/dataLocation /tmp/free2wheelers/stationMart/data


tail -f /dev/null