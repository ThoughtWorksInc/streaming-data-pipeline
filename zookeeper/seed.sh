#!/bin/sh
zk_command="zkCli.sh -server zookeeper:2181"
echo $zk_command
$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/statusinformation ''
$zk_command create /free2wheelers/statusinformation/kafka-brokers kafka:9092
$zk_command create /free2wheelers/statusinformation/topic status_information
$zk_command create /free2wheelers/statusinformation/checkpointLocation file:///Users/chandni/thoughtworks/DataEngineering/temp/checkpoint
$zk_command create /free2wheelers/statusinformation/dataLocation file:///Users/chandni/thoughtworks/DataEngineering/temp/data
