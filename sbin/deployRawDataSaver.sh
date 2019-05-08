#!/usr/bin/env bash
if [[ -z "${BASTION_IP_ADDRESS}" ]]; then
    echo "No bastion ip configured, consider setting the BASTION_IP_ADDRESS environment variable (in circle ci also) ."
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

Host emr-master.bangalore-april-2019.training
    User hadoop

Host *.bangalore-april-2019.training
	ForwardAgent yes
	ProxyCommand ssh ${BASTION_IP_ADDRESS} -W %h:%p 2>/dev/null
	User ec2-user
    StrictHostKeyChecking no
" >> ~/.ssh/config

echo "====SSH Config Updated===="


echo "====Insert app config in zookeeper===="
scp ./zookeeper/seed.sh kafka.bangalore-april-2019.training:/tmp/zookeeper-seed.sh
ssh kafka.bangalore-april-2019.training '
set -e
export hdfs_server="emr-master.bangalore-april-2019.training:8020"
export kafka_server="kafka.bangalore-april-2019.training:9092"
export zk_command="zookeeper-shell localhost:2181"
sh /tmp/zookeeper-seed.sh
'
echo "====Inserted app config in zookeeper===="

echo "====Configure HDFS paths===="
scp ./hdfs/seed.sh emr-master.bangalore-april-2019.training:/tmp/hdfs-seed.sh

ssh emr-master.bangalore-april-2019.training '
set -e
export hdfs_server="emr-master.bangalore-april-2019.training:8020"
export hadoop_path="hadoop"
sh /tmp/hdfs-seed.sh
'
echo "====HDFS paths configured==="

echo "====Copy Raw Data Saver Jar to EMR===="
scp RawDataSaver/target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar emr-master.bangalore-april-2019.training:/tmp/
echo "====Raw Data Saver Jar Copied to EMR===="

scp sbin/go.sh emr-master.bangalore-april-2019.training:/tmp/go.sh

ssh emr-master.bangalore-april-2019.training '
set -e

source /tmp/go.sh

echo "====Kill Old Raw Data Saver===="

kill_application "StationStatusSaverApp"
kill_application "StationInformationSaverApp"
kill_application "StationDataSFSaverApp"

echo "====Old Raw Data Saver Killed===="

echo "====Deploy Raw Data Saver===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationStatusSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.bangalore-april-2019.training:2181 "/free2wheelers/stationStatus" 1>/tmp/raw-station-status-data-saver.log 2>/tmp/raw-station-status-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationInformationSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.bangalore-april-2019.training:2181 "/free2wheelers/stationInformation" 1>/tmp/raw-station-information-data-saver.log 2>/tmp/raw-station-information-data-saver.error.log &

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationLocationApp --name StationDataSFSaverApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0 --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-raw-data-saver_2.11-0.0.1.jar kafka.bangalore-april-2019.training:2181 "/free2wheelers/stationDataSF" 1>/tmp/raw-station-data-sf-saver.log 2>/tmp/raw-station-data-sf-saver.error.log &

echo "====Raw Data Saver Deployed===="
'
