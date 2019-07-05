echo "-----------------------------"
echo "Adding synthetic message for SyntheticBikeStation to station_data_marseille"
# The nested updated_timestamp will only work on Linux distributions, it wont work locally.
ssh -oStrictHostKeyChecking=no -tt kafka.$TRAINING_COHORT.training <<'endOfKafkaCommands'
    updated_timestamp=$(date +"%FT%T.%6NZ")
    kafka-console-producer --broker-list localhost:9092 --topic station_data_marseille <<< ""{\"payload\":{\"network\":{\"company\":[\"TW\"],\"href\":\"/v2/networks/le-velo\",\"id\":\"le-velo\",\"license\":{\"name\":\"OpenLicence\",\"url\":\"https://developer.jcdecaux.com/#/opendata/licence\"},\"location\":{\"city\":\"City\",\"country\":\"US\",\"latitude\":0.00,\"longitude\":0.00},\"name\":\"SyntheticBikeStation\",\"stations\":[{\"empty_slots\":20,\"extra\":{\"address\":\"FakeStreet\",\"banking\":true,\"bonus\":false,\"last_update\":1542234250000,\"slots\":21,\"status\":\"OPEN\",\"uid\":\"syntheticID\"},\"free_bikes\":1,\"id\":\"syntheticID\",\"latitude\":0.00,\"longitude\":0.00,\"name\":\"SyntheticBikeStation\",\"timestamp\":\"$updated_timestamp\"}]}}}""
    logout
endOfKafkaCommands
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Waiting 2 minutes for streaming work to be done"
# Sleep until station_data_sf present consumer offset exceeds stored producer offset. Hardcoded until we can figure it out.
sleep 120
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Retrieving Previous Timestamp"
previous_record=$(ssh -oStrictHostKeyChecking=no -tt emr-master.$TRAINING_COHORT.training 'hadoop fs -cat /free2wheelers/stationMart/data/part*.csv | grep SyntheticBikeStation' )
previous_epoch=$(echo $previous_record | cut -d ',' -f 5 | date +%s)
echo "-----------------------------"
echo ""

# Deletes the test topic on the Kafka stream. Doing this to clean test topics
# In the case where the topic does not exist we will coerce the exit code to 0 so that execution continues
# Creates a topic on the Kafka machine. Assumes that Station Consumer will accept any topic named 'station_data_*'
# Put message on kafka queue
echo "-----------------------------"
echo "Adding synthetic message for SyntheticBikeStation to station_data_marseille"
ssh -oStrictHostKeyChecking=no -tt kafka.$TRAINING_COHORT.training <<'endOfKafkaCommands'
    updated_timestamp=$(date +"%FT%T.%6NZ")
    kafka-console-producer --broker-list localhost:9092 --topic station_data_marseille <<< ""{\"payload\":{\"network\":{\"company\":[\"TW\"],\"href\":\"/v2/networks/le-velo\",\"id\":\"le-velo\",\"license\":{\"name\":\"OpenLicence\",\"url\":\"https://developer.jcdecaux.com/#/opendata/licence\"},\"location\":{\"city\":\"City\",\"country\":\"US\",\"latitude\":0.00,\"longitude\":0.00},\"name\":\"SyntheticBikeStation\",\"stations\":[{\"empty_slots\":20,\"extra\":{\"address\":\"FakeStreet\",\"banking\":true,\"bonus\":false,\"last_update\":1542234250000,\"slots\":21,\"status\":\"OPEN\",\"uid\":\"syntheticID\"},\"free_bikes\":1,\"id\":\"syntheticID\",\"latitude\":0.00,\"longitude\":0.00,\"name\":\"SyntheticBikeStation\",\"timestamp\":\"$updated_timestamp\"}]}}}""
    logout
endOfKafkaCommands
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Waiting 2 minutes for streaming work to be done"
# Sleep until station_data_sf present consumer offset exceeds stored producer offset. Hardcoded until we can figure it out.
sleep 120
echo "-----------------------------"
echo ""

echo "-----------------------------"
echo "Retrieving Actual Timestamp"
actual_record=$(ssh -oStrictHostKeyChecking=no -tt emr-master.$TRAINING_COHORT.training 'hadoop fs -cat /free2wheelers/stationMart/data/part*.csv | grep SyntheticBikeStation' )
actual_epoch=$(echo $actual_record | cut -d ',' -f 5 | date +%s)
echo "-----------------------------"
echo ""

if (( $actual_epoch > $previous_epoch ));then
    echo "Actual timestamp is greater than previous timestamp; record has been updated"    
    echo "Everything is Awesome"
else
    echo "Previous record is ${previous_record}"
    echo "Actual record is ${actual_record}"
    echo "Shit's on fire"
    exit 1
fi