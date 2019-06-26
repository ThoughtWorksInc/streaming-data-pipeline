# streaming-data-pipeline

[![CircleCI](https://circleci.com/gh/ThoughtWorksInc/streaming-data-pipeline.svg?style=svg)](https://circleci.com/gh/ThoughtWorksInc/streaming-data-pipeline)

Streaming pipeline repo for data engineering training program

See producers and consumers set up README in their respective directories

#local environment setup

###Prerequisites:
- Make sure you have sbt installed.
- Make sure you have docker installed and running.

1. Run `./sbin/buildAndRunLocal.sh`. This creates various Docker containers (each with an independent purpose) for running and testing this setup on your local machine.

2. If everything us up and running, you should be able to see data in hadoop.
To check for data:
    1. `docker ps | grep hadoop` - you should see at least one container referencing hadoop (we can ignore hadoop_seed for now)
    2. `docker exec -it $CONTAINER_ID bash`
    3. `/usr/local/hadoop/bin/hadoop fs -ls /free2wheelers/stationMart/data`
    4. Tada! We have data! (if you don't -- something went wrong, check "Considerations")

###Considerations
- Your docker machine may need CPUs: 2/Memory: 4GiB/Swap: 512 MiB; remember to "Apply & Restart"
- When running the script run `docker stats` for some insights
- There's a script for stopping: `./sbin/stopAndRemoveLocal.sh`, try stopping and restarting
- Make sure you don't have a previous instance of Zookeeper, Kafka or Spark running before executing the script (it won't be able to allocate the port)


### A Second Option [Deprecated]:
In order to run the streaming application in a single Docker image, you have to run inside each folder before run the docker container:

sbt assembly

To run the docker container you have available this commands:

make run => that run the container with all infrastructure needed 

make kill => kill the docker container

make console => access to the console of the docker container

