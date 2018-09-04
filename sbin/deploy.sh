#!/usr/bin/env bash

set -e

echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
    StrictHostKeyChecking no

Host emr-master.xian-summer-2018.training
    User hadoop

Host *.xian-summer-2018.training
	ForwardAgent yes
	ProxyCommand ssh 13.229.192.72 -W %h:%p 2>/dev/null
	User ec2-user
    StrictHostKeyChecking no
" >> ~/.ssh/config

echo "====SSH Config Updated===="

echo "====Insert app config in zookeeper===="
scp ./zookeeper/seed.sh kafka.xian-summer-2018.training:~/
ssh kafka.xian-summer-2018.training '
set -e
export hdfs_server="emr-master.xian-summer-2018.training:8020"
export kafka_server="kafka.xian-summer-2018.training:9092"
export zk_command="zookeeper-shell localhost:2181"
sh ~/seed.sh
'
echo "====Inserted app config in zookeeper===="

echo "====Copy jar to ingester server===="
scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ingester.xian-summer-2018.training:/tmp/
echo "====Jar copied to ingester server===="

ssh ingester.xian-summer-2018.training '
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


echo "====Kill running producers===="

kill_process ${station_information}
kill_process ${station_status}
kill_process ${station_san_francisco}

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="

nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_information} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/tmp/${station_information}.log 2>/tmp/${station_information}.error.log &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_san_francisco} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/tmp/${station_san_francisco}.log 2>/tmp/${station_san_francisco}.error.log &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_status} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/tmp/${station_status}.log 2>/tmp/${station_status}.error.log &

echo "====Producers Deployed===="
'


echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar emr-master.xian-summer-2018.training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="

scp sbin/go.sh emr-master.xian-summer-2018.training:/tmp/go.sh

ssh emr-master.xian-summer-2018.training '
set -e

sourch /tmp/go.sh

echo "====Kill Old Raw Data Saver===="

kill_application "com.free2wheelers.apps.StationLocationApp"

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.xian-summer-2018.training:2181 1>/tmp/raw-data-saver.log 2>/tmp/raw-data-saver.error.log &

echo "====Raw Data Saver Deployed===="
'


echo "====Copy Station Consumer Jar to EMR===="
scp StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar emr-master.xian-summer-2018.training:/tmp/
echo "====Station Consumer Jar Copied to EMR===="

scp sbin/go.sh emr-master.xian-summer-2018.training:/tmp/go.sh

ssh emr-master.xian-summer-2018.training '
set -e

sourch /tmp/go.sh


echo "====Kill Old Station Consumer===="

kill_application "com.free2wheelers.apps.StationApp"

echo "====Old Station Consumer Killed===="

echo "====Deploy Station Consumer===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-station-consumer_2.11-0.0.1.jar kafka.xian-summer-2018.training:2181 1>/tmp/station-consumer.log 2>/tmp/station-consumer.error.log &

echo "====Station Consumer Deployed===="
'
