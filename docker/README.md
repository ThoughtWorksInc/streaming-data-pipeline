# Local environment setup

0. Verify that you don't have Zookeeper or Kafka running locally (ports 9092 and 2181 are free)
0. Verify that your Docker Daemon is running
0. Build the application JARs and put them in the correct directories (see `./sbin/buildAndRunLocal.sh` to find out where each file should be copied)
0. Run the following command from the `docker` directory
    ```
    ./docker-compose.sh up -d
    ```
    
    This  will start 5 containers:
    
        - one for Kafka (exposed on Mac port 9092)
        - one for Zookeeper (exposed on Mac port 2181)
        - one for the station status producer
        - one for the station information producer
        - one for HDFS
0. Verify that Kafka is up and running by connecting to the broker and verifying that you can receive a messages on the `station_status` topic
    0. Method one -- remote into the Kafka Docker container
        0. Find the ID of the container running Kafka
        0. Remote in to that machine
        
            ```
            docker exec -it <CONTAINER_ID> /bin/bash
            ```
        0. Use the container's `kafka-console-consumer.sh` to consume from the `station_status` topic
        
            ```
            kafka-console-consumer.sh --zookeeper zookeeper:2181 --topic station_status
            ```
        
    0. Method two -- configure your Mac to be able to connect to the Docker Kafka instance
        0. Edit `/etc/hosts` and add the following two lines
        
           ```
           127.0.0.1 kafka
           127.0.0.1 zookeeper
           ```
           This will let your machine know that it can reach Kafka and Zookeeper on localhost (127.0.0.1) when Kafka advertises its hostname as `kafka`
        0. Use your local `kafka-console-consumer` to consume from the `station_status` topic
            
            ```
            kafka-console-consumer --bootstrap-server localhost:9092 --topic station_status
            
            ```

0. Verify that the Zookeeper port is available (this assumes you have a local Zookeeper installation
    0. Run `zkCli -server localhost:2181` to connect to the Zookeeper instance
    0. Verify that the following command in `zkCli` works: `ls /`
    0. Type `quit` to exit the Zookeeper CLI

## Test connect to zookeeper in containers

  `docker exec -it docker_zookeeper_1 zkCli.sh -server localhost:2181`

## Test connection to Hadoop in containers
  `docker exec -it streamingdatapipeline_hadoop_1 /usr/local/hadoop/bin/hadoop fs -ls /` 
  `curl http://localhost:50070`
  `curl http://localhost:8088/cluster`
