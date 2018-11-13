# Get last updated time from HDFS. Assumes a synthetic record exists.

echo "-----------------------------"
echo "Retrieving Previous Timestamp"
previous_timestamp=$(ssh -tt emr-master.$TRAINING_COHORT.training 'hadoop fs -cat /free2wheelers/stationMart/data/part*.csv | grep Lefferts | cut -d "," -f 5')
previous_without_trail=${previous_timestamp%$'\r'}
echo "-----------------------------"
echo ""

# Deletes the test topic on the Kafka stream. Doing this to clean test topics
# In the case where the topic does not exist we will coerce the exit code to 0 so that execution continues
# Creates a topic on the Kafka machine. Assumes that Station Consumer will accept any topic named 'station_data_*'
# Put message on kafka queue
echo "-----------------------------"
echo "Adding synthetic message to test topic"
ssh -tt kafka.$TRAINING_COHORT.training <<'endOfKafkaCommands'
    updated_timestamp=$(date +%s)
    kafka-console-producer --broker-list localhost:9092 --topic station_data_nyc <<< ""{\"station_id\":\"synthetic_id\",\"bikes_available\":30,\"docks_available\":7,\"is_renting\":true,\"is_returning\":true,\"last_updated\":$updated_timestamp,\"name\":\"SyntheticBikeStation\",\"latitude\":0.00,\"longitude\":0.00}""
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
actual_timestamp=$(ssh -tt emr-master.$TRAINING_COHORT.training 'hadoop fs -cat /free2wheelers/stationMart/data/part*.csv | grep Lefferts | cut -d "," -f 5');
actual_without_trail=${actual_timestamp%$'\r'}
echo "-----------------------------"
echo ""

if (( $actual_without_trail > $previous_without_trail ));then
    echo "Actual timestamp is greater than previous timestamp; record has been updated"    
    echo "Everything is Awesome"
else
    echo "Previous Timestamp is ${previous_without_trail}, Actual Timestamp is ${actual_without_trail}"
    echo "Shit's on fire"
    exit 1
fi