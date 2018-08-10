#!/usr/bin/env bash

set -e

echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
    StrictHostKeyChecking no

Host *.xian-summer-2018.training
	ForwardAgent yes
    StrictHostKeyChecking no
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

echo "====Kill running producers===="

kill_process ${station_information}
kill_process ${station_status}

echo "====Runing Producers Killed===="

echo "====Deploy Producers===="

nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_information} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &
nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=${station_status} --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &

echo "====Producers Deployed===="
'

