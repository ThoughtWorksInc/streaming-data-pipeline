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
	ProxyCommand ssh 13.251.252.122 -W %h:%p 2>/dev/null
	User ec2-user
    StrictHostKeyChecking no
" >> ~/.ssh/config

echo "====SSH Config Updated===="

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
station_information-san_francisco = "producer_station_information-san_francisco"


echo "====Kill running producers===="

kill_process ${station_information}
kill_process ${station_status}
kill_process ${station_information-san_francisco}

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="

nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_information} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_information-san_francisco} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_status} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &

echo "====Producers Deployed===="
'


echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar emr-master.xian-summer-2018.training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="


ssh emr-master.xian-summer-2018.training '
set -e

function kill_process {
    query=$1
    pid=`ps aux | grep $query | grep -v "grep" |  awk "{print \\$2}"`

    if [ -z "$pid" ];
    then
        echo "no ${query} process running"
    else
        kill -SIGTERM $pid
    fi
}

raw_data_saver="free2wheelers-raw-data-saver"

echo "====Kill Old Raw Data Saver===="

kill_process ${raw_data_saver}

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --class com.free2wheelers.apps.StationLocationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar 1>/dev/null 2>/dev/null &

echo "====Raw Data Saver Deployed===="
'


echo "====Copy Station Consumer Jar to EMR===="
scp StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar emr-master.xian-summer-2018.training:/tmp/
echo "====Station Consumer Jar Copied to EMR===="


ssh emr-master.xian-summer-2018.training '
set -e

function kill_process {
    query=$1
    pid=`ps aux | grep $query | grep -v "grep" |  awk "{print \\$2}"`

    if [ -z "$pid" ];
    then
        echo "no ${query} process running"
    else
        kill -SIGTERM $pid
    fi
}

station_consumer="free2wheelers-station-consumer"

echo "====Kill Old Station Consumer===="

kill_process ${station_consumer}

echo "====Old Station Consumer Killed===="

echo "====Deploy Station Consumer===="

nohup spark-submit --class com.free2wheelers.apps.StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  /tmp/free2wheelers-station-consumer_2.11-0.0.1.jar kafka.xian-summer-2018.training:2181 1>/dev/null 2>/dev/null &

echo "====Station Consumer Deployed===="
'
