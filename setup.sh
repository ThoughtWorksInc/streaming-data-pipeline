#!/usr/bin/env bash
set -x

HOST_NAME=`hostname`

cat zookeeper.properties

echo $PATH

PATH=$PATH nohup zookeeper-server-start.sh zookeeper.properties 1> /tmp/zookeeper.log  2>/tmp/zookeeper.error.log &

echo "
advertised.host.name=$HOST_NAME" >> kafka.properties
echo "listener.security.protocol.map=PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT" >> kafka.properties
echo "inter.broker.listener.name=PLAINTEXT" >> kafka.properties
echo "listeners=PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092" >> kafka.properties
echo "advertised.listeners=PLAINTEXT://$HOST_NAME:29092,PLAINTEXT_HOST://localhost:9092" >> kafka.properties

nohup kafka-server-start.sh kafka.properties 1> /tmp/kafka.log  2>/tmp/kafka.error.log &

start-master.sh -p 7077

start-slave.sh 127.0.0.1:7077

mkdir -p /tmp/spark-events && start-history-server.sh

mkdir -p /tmp/free2wheelers/rawData/stationInformation/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationInformation/data
mkdir -p /tmp/free2wheelers/rawData/stationStatus/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationStatus/data
mkdir -p /tmp/free2wheelers/rawData/stationSanFrancisco/checkpoints
mkdir -p /tmp/free2wheelers/rawData/stationSanFrancisco/data

mkdir -p /tmp/free2wheelers/stationMart/checkpoints
mkdir -p /tmp/free2wheelers/stationMart/data

zk_command="zookeeper-shell.sh 127.0.0.1:2181"

$zk_command create /free2wheelers ''
$zk_command create /free2wheelers/stationInformation ''
$zk_command create /free2wheelers/stationInformation/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationInformation/topic station_information
$zk_command create /free2wheelers/stationInformation/checkpointLocation /tmp/free2wheelers/rawData/stationInformation/checkpoints
$zk_command create /free2wheelers/stationInformation/dataLocation /tmp/free2wheelers/rawData/stationInformation/data

$zk_command create /free2wheelers/stationStatus ''
$zk_command create /free2wheelers/stationStatus/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationStatus/topic station_status
$zk_command create /free2wheelers/stationStatus/checkpointLocation /tmp/free2wheelers/rawData/stationStatus/checkpoints
$zk_command create /free2wheelers/stationStatus/dataLocation /tmp/free2wheelers/rawData/stationStatus/data

$zk_command create /free2wheelers/stationDataSF ''
$zk_command create /free2wheelers/stationDataSF/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationDataSF/topic station_data_sf
$zk_command create /free2wheelers/stationDataSF/checkpointLocation /tmp/free2wheelers/rawData/stationDataSF/checkpoints
$zk_command create /free2wheelers/stationDataSF/dataLocation /tmp/free2wheelers/rawData/stationDataSF/data

$zk_command create /free2wheelers/stationDataNYC ''
$zk_command create /free2wheelers/stationDataNYC/kafkaBrokers 127.0.0.1:9092
$zk_command create /free2wheelers/stationDataNYC/topic station_data_nyc
$zk_command create /free2wheelers/stationDataNYC/checkpointLocation /tmp/free2wheelers/rawData/stationDataNYC/checkpoints
$zk_command create /free2wheelers/stationDataNYC/dataLocation /tmp/free2wheelers/rawData/stationDataNYC/data

$zk_command create /free2wheelers/output ''
$zk_command create /free2wheelers/output/checkpointLocation /tmp/free2wheelers/stationMart/checkpoints
$zk_command create /free2wheelers/output/dataLocation /tmp/free2wheelers/stationMart/data

mkdir -p /tmp/apps/

echo "====Deploy Producers===="
 
nohup java -jar /apps/CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=station-information --kafka.brokers=$HOST_NAME:29092 1>/tmp/apps/station-information.log 2>/tmp/apps/station-information.error.log &

nohup java -jar /apps/CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=station-san-francisco --kafka.brokers=$HOST_NAME:29092 1>/tmp/apps/station-san-francisco.log 2>/tmp/apps/station-san-francisco.error.log &

nohup java -jar /apps/CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=station-status --kafka.brokers=$HOST_NAME:29092 1>/tmp/apps/station-status.log 2>/tmp/apps/station-status.error.log &
 
 echo "====Producers Deployed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master spark://$HOST_NAME:7077 --class com.free2wheelers.apps.StationLocationApp --name StationStatusSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 1024M --conf spark.executor.memory=2g --conf spark.cores.max=1 /apps/RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar localhost:2181 "/free2wheelers/stationStatus" 1>/tmp/apps/raw-station-status-data-saver.log 2>/tmp/apps/raw-station-status-data-saver.error.log &

nohup spark-submit --master spark://$HOST_NAME:7077 --class com.free2wheelers.apps.StationLocationApp --name StationInformationSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 1024M --conf spark.executor.memory=2g --conf spark.cores.max=1 /apps/RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar localhost:2181 "/free2wheelers/stationInformation" 1>/tmp/apps/raw-station-information-data-saver.log 2>/tmp/apps/raw-station-information-data-saver.error.log &

nohup spark-submit --master spark://$HOST_NAME:7077 --class com.free2wheelers.apps.StationLocationApp --name StationDataSFSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 1024M --conf spark.executor.memory=2g --conf spark.cores.max=1 /apps/RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar localhost:2181 "/free2wheelers/stationDataSF" 1>/tmp/apps/raw-station-data-sf-saver.log 2>/tmp/apps/raw-station-data-sf-saver.error.log &

echo "====Raw Data Saver Deployed===="

echo "====Deploy Station Consumers===="

nohup spark-submit --master spark://$HOST_NAME:7077 --class com.free2wheelers.apps.StationApp --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 1024M --conf spark.executor.memory=2g --conf spark.cores.max=1 /apps/StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar localhost:2181 1>/tmp/apps/station-consumer.log 2>/tmp/apps/station-consumer.error.log &

nohup spark-submit --master spark://$HOST_NAME:7077 --class com.free2wheelers.apps.StationApp --name StationTransformerNYC --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 1024M --conf spark.executor.memory=2g --conf spark.cores.max=1 /apps/StationTransformerNYC/target/scala-2.11/free2wheelers-station-transformer-nyc_2.11-0.0.1.jar localhost:2181 1>/tmp/apps/station-transformer-nyc.log 2>/tmp/apps/station-transformer-nyc.error.log &

echo "====Station Consumers Deployed===="
 
tail -f /dev/null
