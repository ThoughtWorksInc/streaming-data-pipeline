#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

./sbin/buildProducer.sh && ./sbin/buildRawDataSaver.sh && ./sbin/buildStationConsumer.sh \
    && ./sbin/buildStationTransformer.sh && ./sbin/buildMonitoringApp.sh

echo "====Running docker-compose===="
$DIR/../docker/docker-compose.sh --project-directory $DIR/../docker -f $DIR/../docker/docker-compose.yml up --build -d

