#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building RawDataSaver ===="
cd $DIR/../RawDataSaver && sbt test && sbt package