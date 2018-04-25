#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Producer JARs===="
$DIR/../CitibikeApiProducer/gradlew -p $DIR/../CitibikeApiProducer clean bootJar
echo "====Building Consumer JARs===="
cd $DIR/../RawDataSaver && sbt package
cd $DIR/../StationConsumer && sbt package
echo "====Running docker-compose===="
$DIR/../docker/docker-compose.sh --project-directory $DIR/../docker -f $DIR/../docker/docker-compose.yml up --build -d

