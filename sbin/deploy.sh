#!/usr/bin/env bash

set -e

echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
    StrictHostKeyChecking no
	ForwardAgent yes
	DynamicForward 6789
Host emr-master.twdu1.training
    User hadoop
Host *.twdu1.training
	StrictHostKeyChecking no
	ForwardAgent yes
	ProxyCommand ssh 18.139.56.171 -W %h:%p 2>/dev/null
	User ec2-user
" >> ~/.ssh/config

echo "====SSH Config Updated===="

echo "====Insert app config in zookeeper===="
scp ./zookeeper/seed.sh kafka.twdu1.training:/tmp/zookeeper-seed.sh

echo "====Inserted app config in zookeeper===="

echo "====Copy jar to ingester server===="
scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ec2-user@ingester.twdu1.training:/tmp/
echo "====Jar copied to ingester server===="

ssh ec2-user@ingester.twdu1.training '
set -e

function kill_process {
    query=$1
    pid=`ps aux | grep $query | grep -v "grep" |  awk "{print \\$2}"`

    if [ -z "$pid" ];
    then
        echo "no ${query} process running"
    else
        kill -9 $pid
    fi
}

station_information="station-information"
station_status="station-status"
station_san_francisco="station-san-francisco"
station_nyc="station-nyc"
station_marseille="station-marseille"


echo "====Kill running producers===="

kill_process ${station_information}
kill_process ${station_status}
kill_process ${station_san_francisco}
kill_process ${station_nyc}
kill_process ${station_marseille}

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="

nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_san_francisco} --producer.topic=station_data_sf --kafka.brokers=kafka.twdu1.training:9092 1>/tmp/${station_san_francisco}.log 2>/tmp/${station_san_francisco}.error.log &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_nyc} --producer.topic=station_data_nyc_v2 --kafka.brokers=kafka.twdu1.training:9092 1>/tmp/${station_nyc}.log 2>/tmp/${station_nyc}.error.log &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_marseille} --kafka.brokers=kafka.twdu1.training:9092 1>/tmp/${station_marseille}.log 2>/tmp/${station_marseille}.error.log &

echo "====Producers Deployed===="
'


echo "====Configure HDFS paths===="
scp ./hdfs/seed.sh hadoop@emr-master.twdu1.training:/tmp/hdfs-seed.sh

ssh hadoop@emr-master.twdu1.training '
set -e
export hdfs_server="emr-master.twdu1.training:8020"
export hadoop_path="hadoop"
sh /tmp/hdfs-seed.sh
'

echo "====HDFS paths configured==="


echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar hadoop@emr-master.twdu1training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="

scp sbin/go.sh hadoop@emr-master.twdu1.training:/tmp/go.sh

ssh hadoop@emr-master.twdu1.training '
set -e

source /tmp/go.sh

echo "====Kill Old Raw Data Saver===="

kill_application "StationStatusSaverApp"
kill_application "StationInformationSaverApp"
kill_application "StationDataSFSaverApp"

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationStatusSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.twdu1.training:2181 "/free2wheelers/stationStatus" 1>/tmp/raw-station-status-data-saver.log 2>/tmp/raw-station-status-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationInformationSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.twdu1.training:2181 "/free2wheelers/stationInformation" 1>/tmp/raw-station-information-data-saver.log 2>/tmp/raw-station-information-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationDataSFSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.twdu1.training:2181 "/free2wheelers/stationDataSF" 1>/tmp/raw-station-data-sf-saver.log 2>/tmp/raw-station-data-sf-saver.error.log &

echo "====Raw Data Saver Deployed===="
'


echo "====Copy Station Consumers Jar to EMR===="
scp StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar hadoop@emr-master.twdu1.training:/tmp/
echo "====Station Consumers Jar Copied to EMR===="

echo "====Copy File Checker Jar to EMR===="
scp FileChecker/target/scala-2.11/free2wheelers-file-checker_2.11-0.0.1.jar hadoop@emr-master.twdu1.training:/tmp/
echo "====File Checker Jar Copied to EMR===="

scp sbin/go.sh hadoop@emr-master.twdu1.training:/tmp/go.sh

ssh hadoop@emr-master.twdu1.training '
set -e

source /tmp/go.sh


echo "====Kill Old Station Consumer===="

kill_application "StationApp"

echo "====Old Station Consumer Killed===="

echo "====Kill Old File Checker===="

kill_application "FileCheckerApp"

echo "====Old File Checker Killed===="

echo "====Deploy Station Consumer===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationApp --queue streaming --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 500M --conf spark.executor.memory=1g --conf spark.cores.max=1 /tmp/free2wheelers-station-consumer_2.11-0.0.1.jar kafka.twdu1.training:2181 1>/tmp/station-consumer.log 2>/tmp/station-consumer.error.log &

echo "====Station Consumer Deployed===="

'

echo "====copy dags to airflow machine===="
scp -r ./airflow/dags ec2-user@airflow.twdu1.training:~/airflow/
echo "====dags copied to airflow machine===="
