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

scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ingester.xian-summer-2018.training:/tmp/
ssh ingester.xian-summer-2018.training 'nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=station-information --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &'
ssh ingester.xian-summer-2018.training 'nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar --spring.profiles.active=station-status --kafka.brokers=kafka.xian-summer-2018.training:9092 1>/dev/null 2>/dev/null &'

