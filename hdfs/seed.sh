!/bin/bash

/usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/checkpoints \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationInformation/data \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/checkpoints \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationStatus/data \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationSanFrancisco/checkpoints \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/rawData/stationSanFrancisco/data \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/checkpoints \
&& /usr/local/hadoop/bin/hadoop fs -fs hdfs://$hdfs_server -mkdir -p /free2wheelers/stationMart/data
