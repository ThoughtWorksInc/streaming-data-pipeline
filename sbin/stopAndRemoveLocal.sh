#!/usr/bin/env bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

echo "====Running docker-compose down===="
$DIR/../docker/docker-compose-down.sh --project-directory $DIR/../docker -f $DIR/../docker/docker-compose.yml down
