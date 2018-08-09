#!/usr/bin/env bash

set -e

echo "====Deploying Producers===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
  StrictHostKeyChecking no

Host emr-master.xian-summer-2018.training
	ForwardAgent yes
  StrictHostKeyChecking no
	ProxyCommand ssh 13.251.252.122 -W %h:%p 2>/dev/null
	User hadoop
  StrictHostKeyChecking no
" >> ~/.ssh/config

scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar emr-master.xian-summer-2018.training:/tmp/
