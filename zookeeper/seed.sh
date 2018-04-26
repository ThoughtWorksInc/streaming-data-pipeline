#!/bin/sh
zk_command="zkCli.sh -server zookeeper:2181"
echo $zk_command
$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/stationInformation ''
$zk_command create /free2wheelers/stationInformation/kafkaBrokers kafka:9092
$zk_command create /free2wheelers/stationInformation/topic station_information
$zk_command create /free2wheelers/stationInformation/checkpointLocation hdfs://hadoop:9000/free2wheelers/rawData/stationInformation/checkpoints
$zk_command create /free2wheelers/stationInformation/dataLocation hdfs://hadoop:9000/free2wheelers/rawData/stationInformation/data

$zk_command create /free2wheelers/stationStatus ''
$zk_command create /free2wheelers/stationStatus/kafkaBrokers kafka:9092
$zk_command create /free2wheelers/stationStatus/topic station_status
$zk_command create /free2wheelers/stationStatus/checkpointLocation hdfs://hadoop:9000/free2wheelers/rawData/stationStatus/checkpoints
$zk_command create /free2wheelers/stationStatus/dataLocation hdfs://hadoop:9000/free2wheelers/rawData/stationStatus/data

$zk_command create /free2wheelers/output ''
$zk_command create /free2wheelers/output/checkpointLocation hdfs://hadoop:9000/free2wheelers/stationMart/checkpoints
$zk_command create /free2wheelers/output/dataLocation hdfs://hadoop:9000/free2wheelers/stationMart/data
