#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Monitoring JARs===="

cd $DIR/../Monitoring && sbt test && sbt package