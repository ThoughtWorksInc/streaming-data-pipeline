#!/usr/bin/env bash

set -e

echo "====Deploying Producers===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789

Host *.xian-summer-2018.training
	User ec2-user
	ForwardAgent yes
	ProxyCommand ssh 13.251.252.122 -W %h:%p 2>/dev/null

Host emr-master.xian-summer-2018.training
	User hadoop
" >> ~/.ssh/config

scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar emr-master.xian-summer-2018.training:/tmp/
