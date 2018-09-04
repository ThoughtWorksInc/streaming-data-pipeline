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