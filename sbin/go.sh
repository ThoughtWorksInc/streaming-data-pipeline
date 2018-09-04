#!/usr/bin/env bash

set -e

function kill_application {
	applicationName=$1
	applicationIds=`yarn application -list | awk -v name=$applicationName 'match($2,name){print $1}'`

	for applicationId in $applicationIds;
	do
		echo "Kill ${applicationName} with applicationId ${applicationId}"
		yarn application -kill  $applicationId
	done
}

function kill_process {
	query=$1
	pids=`ps aux | grep $query | grep -v "grep" |  awk "{print $2}"`

	for pid in $applicationIds;
	do
		echo "Kill $query with pid ${pid}"
		kill -SIGTERM $pid
		yarn application -kill  $i
	done
}