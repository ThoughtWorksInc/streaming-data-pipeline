#!/usr/bin/env bash

set -e

echo "====Deploying Producers===="
scp CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar ec2-user@13.251.252.122:/tmp
ssh ec2-user@ec2-user@13.251.252.122 'scp /tmp/free2wheelers-citibike-apis-producer0.1.0.jar hadoop@emr_master.xian-summer-2018.training:/tmp'
