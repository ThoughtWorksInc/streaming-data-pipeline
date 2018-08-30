This directory contains the docker file and
seed file that will be used to start hdfs locally.

To access data in hdfs when you are running this docker container
```
docker exec -it streamingdatapipeline_hadoop_1 /bin/bash
```
and then you can look at files by running
```
/usr/local/hadoop/bin/hadoop fs -ls /free2wheelers/stationMart/data
```
