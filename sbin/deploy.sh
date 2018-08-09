#!/usr/bin/env bash

set -e

echo "====Deploying Producers===="

echo "
	User ec2-user
	IdentitiesOnly yes
	ForwardAgent yes
	DynamicForward 6789
  StrictHostKeyChecking no

Host ec2-13-229-103-178.ap-southeast-1.compute.amazonaws.com
	ForwardAgent yes
  StrictHostKeyChecking no
	ProxyCommand ssh 13.251.252.122 -W %h:%p 2>/dev/null
	User hadoop
  StrictHostKeyChecking no
" >> ~/.ssh/config

scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ec2-13-229-103-178.ap-southeast-1.compute.amazonaws.com:/tmp/
ssh ec2-13-229-103-178.ap-southeast-1.compute.amazonaws.com 'nohup java -jar /tmp/free2wheelers-citibike-apis-producer0.1.0.jar 1>/dev/null 2>/dev/null &'

