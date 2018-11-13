#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Producer JARs===="
$DIR/../CitibikeApiProducer/gradlew -p $DIR/../CitibikeApiProducer clean test bootJar
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

echo "====Building Consumer JARs===="
cd $DIR/../RawDataSaver && sbt test package
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

cd $DIR/../StationConsumer && sbt test package
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi

cd $DIR/../StationTransformerNYC && sbt test package
rc=$?; if [[ $rc != 0 ]]; then exit $rc; fi
