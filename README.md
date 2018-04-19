# streaming-data-pipeline
Streaming pipeline repo for data engineering training program

See producers and consumers set up README in their respective directories.

# local environment setup

  `cd docker`  
  `./docker-compose.sh up -d`

This  will start 2 containers, one for Kafka and one for Zookeeper. From your Mac, you'll be able to access these on localhost ports 9092 (Kafka) and 2181 (Zookeeper)
