#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building StationConsumer===="
cd $DIR/../StationConsumer && sbt test && sbt package
