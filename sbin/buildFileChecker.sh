#!/usr/bin/env bash
set -e
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Building File Checker ===="
cd $DIR/../FileChecker && sbt test && sbt package
