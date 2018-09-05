#!/bin/bash

set -e

$hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationSanFrancisco/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationSanFrancisco/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/data
