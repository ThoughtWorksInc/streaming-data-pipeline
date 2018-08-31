#!/bin/sh
echo $zk_command
$zk_command rmr /free2wheelers

$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/stationInformation ''
$zk_command create /free2wheelers/stationInformation/kafkaBrokers $kafka_server

$zk_command create /free2wheelers/stationInformation/topic station_information,station_san_francisco
$zk_command create /free2wheelers/stationInformation/checkpointLocation hdfs://$hdfs_server/free2wheelers/rawData/stationInformation/checkpoints
$zk_command create /free2wheelers/stationInformation/dataLocation hdfs://$hdfs_server/free2wheelers/rawData/stationInformation/data

$zk_command create /free2wheelers/stationStatus/kafkaBrokers $kafka_server
$zk_command create /free2wheelers/stationStatus/topic station_status,station_san_francisco
$zk_command create /free2wheelers/stationStatus/checkpointLocation hdfs://$hdfs_server/free2wheelers/rawData/stationStatus/checkpoints
$zk_command create /free2wheelers/stationStatus/dataLocation hdfs://$hdfs_server/free2wheelers/rawData/stationStatus/data

$zk_command create /free2wheelers/output ''
$zk_command create /free2wheelers/output/checkpointLocation hdfs://$hdfs_server/free2wheelers/stationMart/checkpoints
$zk_command create /free2wheelers/output/dataLocation hdfs://$hdfs_server/free2wheelers/stationMart/data
