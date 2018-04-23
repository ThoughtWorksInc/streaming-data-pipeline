#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Producer JARs===="
$DIR/../CitibikeApiProducer/gradlew -p $DIR/../CitibikeApiProducer clean bootJar
echo "====Copying Producer JARs to Docker directories===="
cp $DIR/../CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar $DIR/../docker/stationStatus/app.jar
cp $DIR/../CitibikeApiProducer/build/libs/free2wheelers-citibike-apis-producer0.1.0.jar $DIR/../docker/stationInformation/app.jar
echo "====Running docker-compose===="
$DIR/../docker/docker-compose.sh --project-directory $DIR/../docker -f $DIR/../docker/docker-compose.yml up --build -d
