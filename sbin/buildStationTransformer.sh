#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building Station Transformer ===="
cd $DIR/../StationTransformerNYC && sbt test && sbt package
