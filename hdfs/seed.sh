#!/bin/bash
until /usr/local/hadoop/bin/hadoop fs -test -e hdfs://hadoop:9000/*; do
  echo "Waiting for HDFS name node"
  sleep 1
done

/usr/local/hadoop/bin/hadoop fs -fs hdfs://hadoop:9000 -mkdir -p /free2wheelers/rawData/stationInformation/checkpoints
/usr/local/hadoop/bin/hadoop fs -fs hdfs://hadoop:9000 -mkdir -p /free2wheelers/rawData/stationInformation/data

/usr/local/hadoop/bin/hadoop fs -fs hdfs://hadoop:9000 -mkdir -p /free2wheelers/stationMart/checkpoints
/usr/local/hadoop/bin/hadoop fs -fs hdfs://hadoop:9000 -mkdir -p /free2wheelers/stationMart/data


echo "HDFS seed finished"