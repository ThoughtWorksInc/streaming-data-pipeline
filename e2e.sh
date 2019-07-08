#!/usr/bin/env bash
echo "====Updating SSH Config===="

echo "
	User ec2-user
	IdentitiesOnly yes
    StrictHostKeyChecking no
	ForwardAgent yes
	DynamicForward 6789
Host emr-master.twdu1-uat.training
    User hadoop
Host *.twdu1-uat.training
	StrictHostKeyChecking no
	ForwardAgent yes
	ProxyCommand ssh 18.139.38.127 -W %h:%p 2>/dev/null
	User ec2-user
" >> ~/.ssh/config

TIME_TO_WAIT_FOR_PROCESS=120
STATION_TOPIC=station_data_marseille
function getFileInformation (){
    local record
    record=$(ssh -oStrictHostKeyChecking=no -tt emr-master.$TRAINING_COHORT.training 'hadoop fs -cat /free2wheelers/stationMart/data/part*.csv | grep SyntheticBikeStation')
    if [[ $? -ne 0 ]]; then
        echo "Is not possible get the file information from HDFS"
        exit 1
    fi
    local epoch
    epoch=$(echo $record | cut -d ',' -f 5 | date +%s)
    if [[ $? -ne 0 ]]; then
        echo "Is not possible get the timestamp from the CSV"
        exit 1
    fi
    epoch
}

function publishMessage(){
ssh -oStrictHostKeyChecking=no -tt kafka.$TRAINING_COHORT.training <<'endOfKafkaCommands'
updated_timestamp=$(date +"%FT%T.%6NZ")
kafka-console-producer --broker-list localhost:9092 --topic station_data_marseille <<< ""{\"payload\":{\"network\":{\"company\":[\"TW\"],\"href\":\"/v2/networks/le-velo\",\"id\":\"le-velo\",\"license\":{\"name\":\"OpenLicence\",\"url\":\"https://developer.jcdecaux.com/#/opendata/licence\"},\"location\":{\"city\":\"City\",\"country\":\"US\",\"latitude\":0.00,\"longitude\":0.00},\"name\":\"SyntheticBikeStation\",\"stations\":[{\"empty_slots\":20,\"extra\":{\"address\":\"FakeStreet\",\"banking\":true,\"bonus\":false,\"last_update\":1542234250000,\"slots\":21,\"status\":\"OPEN\",\"uid\":\"syntheticID\"},\"free_bikes\":1,\"id\":\"syntheticID\",\"latitude\":0.00,\"longitude\":0.00,\"name\":\"SyntheticBikeStation\",\"timestamp\":\"$updated_timestamp\"}]}}}""
logout
endOfKafkaCommands
    if [[ $? -ne 0 ]]; then
        echo "Is not possible publish in Kafka"
        exit 1
    fi
}

function validateVariables(){
    if [[ -z "$TRAINING_COHORT" ]]
    then
      echo "\$TRAINING_COHORT is empty"
      exit 1
    else
        echo "Working with TRAINING_COHORT: $TRAINING_COHORT"
    fi
}

function waitForCompleteProcess(){
    echo "Waiting ${TIME_TO_WAIT_FOR_PROCESS} seconds for streaming work to be done"
    sleep ${TIME_TO_WAIT_FOR_PROCESS}
}

echo "-----------------------------"
echo "E2E Test"
echo "-----------------------------"
validateVariables
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Adding synthetic message for SyntheticBikeStation to ${STATION_TOPIC}"
# The nested updated_timestamp will only work on Linux distributions, it wont work locally.
publishMessage
echo "-----------------------------"
echo ""

echo "-----------------------------"
waitForCompleteProcess
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Retrieving Previous Timestamp"
previous_epoch=$(getFileInformation)
echo "-----------------------------"
echo ""

# Deletes the test topic on the Kafka stream. Doing this to clean test topics
# In the case where the topic does not exist we will coerce the exit code to 0 so that execution continues
# Creates a topic on the Kafka machine. Assumes that Station Consumer will accept any topic named 'station_data_*'
# Put message on kafka queue
echo "-----------------------------"
echo "Adding synthetic message for SyntheticBikeStation to ${STATION_TOPIC}"
publishMessage
echo "-----------------------------"
echo ""

echo "-----------------------------"
waitForCompleteProcess
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Retrieving Actual Timestamp"
actual_record=$(getFileInformation)
echo "-----------------------------"
echo ""
echo "ACTUAL EPOCH: ${actual_epoch}"
echo "PREVIOUS EPOCH: ${previous_epoch}"

if [[ ${actual_epoch} > ${previous_epoch} ]]
then
    echo "Actual timestamp is greater than previous timestamp; record has been updated"
    echo "Everything is Awesome"
else
    echo "Previous record is ${previous_record}"
    echo "Actual record is ${actual_record}"
    echo "Shit's on fire"
    exit 1
fi
