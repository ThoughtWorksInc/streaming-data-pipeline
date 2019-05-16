#!/bin/bash

set -e

$hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationDataSF/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationDataSF/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -rm -r -f /free2wheelers/rawData/stationDataNYC/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationDataNYC/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -rm -r -f /free2wheelers/stationMart/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/data \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationDataFrance/checkpoints \
&& $hadoop_path fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationDataFrance/data
