#!/bin/sh
zk_command="zkCli.sh -server zookeeper:2181"
echo $zk_command
$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/stationInformation ''
$zk_command create /free2wheelers/stationInformation/kafka-brokers kafka:9092
$zk_command create /free2wheelers/stationInformation/topic status_information
$zk_command create /free2wheelers/stationInformation/checkpointLocation hdfs://hadoop:9000/free2wheelers/rawData/stationInformation/checkpoints
$zk_command create /free2wheelers/stationInformation/dataLocation hdfs://hadoop:9000/free2wheelers/rawData/stationInformation/data
