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

echo "====Copy Station Consumers Jar to EMR===="
scp StationConsumer/target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar emr-master.bangalore-april-2019.training:/tmp/

echo "====Station Consumers Jar Copied to EMR===="

scp sbin/go.sh emr-master.bangalore-april-2019.training:/tmp/go.sh

ssh emr-master.bangalore-april-2019.training '
set -e

source /tmp/go.sh


echo "====Kill Old Station Consumers===="

kill_application "StationApp"

echo "====Old Station Consumers Killed===="

echo "====Deploy Station Consumers===="

nohup spark-submit --master yarn --deploy-mode cluster --class com.free2wheelers.apps.StationApp --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  --driver-memory 500M --conf spark.executor.memory=2g --conf spark.cores.max=1 /tmp/free2wheelers-station-consumer_2.11-0.0.1.jar kafka.bangalore-april-2019.training:2181 1>/tmp/station-consumer.log 2>/tmp/station-consumer.error.log &

echo "====Station Consumers Deployed===="
'
