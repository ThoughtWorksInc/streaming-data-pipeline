#!/usr/bin/env bash
if [[ -z "${BASTION_IP_ADDRESS_TEST}" ]]; then
    echo "No bastion ip configured, consider setting the BASTION_IP_ADDRESS_TEST environment variable (in circle ci also) ."
    exit 1
fi

set -e

echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
    StrictHostKeyChecking no

Host emr-master.bangalore-april-2019-test.training
    User hadoop

Host *.bangalore-april-2019-test.training
	ForwardAgent yes
	ProxyCommand ssh ${BASTION_IP_ADDRESS_TEST} -W %h:%p 2>/dev/null
	User ec2-user
    StrictHostKeyChecking no
" >> ~/.ssh/config

echo "====SSH Config Updated===="

echo "====Insert app config in zookeeper===="
scp ./zookeeper/seed.sh kafka.bangalore-april-2019-test.training:/tmp/zookeeper-seed.sh
ssh kafka.bangalore-april-2019-test.training '
set -e
export hdfs_server="emr-master.bangalore-april-2019-test.training:8020"
export kafka_server="kafka.bangalore-april-2019-test.training:9092"
export zk_command="zookeeper-shell localhost:2181"
sh /tmp/zookeeper-seed.sh
'
echo "====Inserted app config in zookeeper===="

echo "====Copy jar to ingester server===="
scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ingester.bangalore-april-2019-test.training:/tmp/
echo "====Jar copied to ingester server===="

ssh ingester.bangalore-april-2019-test.training '
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

station_san_francisco="station-san-francisco-test"

echo "====Kill running producers===="


kill_process ${station_san_francisco}

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="

nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_san_francisco} --producer.topic=station_data_sf --kafka.brokers=kafka.bangalore-april-2019-test.training:9092 1>/tmp/${station_san_francisco}.log 2>/tmp/${station_san_francisco}.error.log &

echo "====Producers Deployed===="
'


echo "====Configure HDFS paths===="
scp ./hdfs/seed.sh emr-master.bangalore-april-2019-test.training:/tmp/hdfs-seed.sh

ssh emr-master.bangalore-april-2019-test.training '
set -e
export hdfs_server="emr-master.bangalore-april-2019-test.training:8020"
export hadoop_path="hadoop"
sh /tmp/hdfs-seed.sh
'

echo "====HDFS paths configured==="


echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar emr-master.bangalore-april-2019-test.training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="

scp sbin/go.sh emr-master.bangalore-april-2019-test.training:/tmp/go.sh

ssh emr-master.bangalore-april-2019-test.training '
set -e

source /tmp/go.sh

echo "====Kill Old Raw Data Saver===="

kill_application "StationDataSFSaverApp"

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --queue streaming --name StationDataSFSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=500m --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.bangalore-april-2019-test.training:2181 "/free2wheelers/stationDataSF" 1>/tmp/raw-station-data-sf-saver.log 2>/tmp/raw-station-data-sf-saver.error.log &

echo "====Raw Data Saver Deployed===="
'


echo "====Copy Station Consumers Jar to EMR===="
scp StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar emr-master.bangalore-april-2019-test.training:/tmp/


scp sbin/go.sh emr-master.bangalore-april-2019-test.training:/tmp/go.sh

ssh emr-master.bangalore-april-2019-test.training '
set -e

source /tmp/go.sh


echo "====Kill Old Station Consumers===="

kill_application "StationApp"

echo "====Old Station Consumers Killed===="

echo "====Deploy Station Consumers===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationApp --queue streaming --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 500M --conf spark.executor.memory=500m --conf spark.cores.max=1 /tmp/free2wheelers-station-consumer_2.11-0.0.1.jar kafka.bangalore-april-2019-test.training:2181 1>/tmp/station-consumer.log 2>/tmp/station-consumer.error.log &

echo "====Station Consumers Deployed===="
'
