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
0. Verify that Kafka is up and running by connecting (from your Mac) to the broker and verifying that you can send and receive a message (this assumes you have a local Kafka installation)
    0. Use `/usr/local/Cellar/kafka/1.0.0/libexec/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic foo` to consume the topic `foo`
    0. In another terminal window, use `/usr/local/Cellar/kafka/1.0.0/libexec/bin/kafka-console-producer.sh --broker-list localhost:9092 --topic foo` to produce to topic `foo` on the Kafka instance
    0. In the producer window, send a message. Verify that the consumer receives it.
0. Verify that the Zookeeper port is available (this assumes you have a local Zookeeper installation
    0. Run `zkCli -server localhost:2181` to connect to the Zookeeper instance
    0. Verify that the following command in `zkCli` works: `ls /`
    0. Type `quit` to exit the Zookeeper CLI

## Test connect to zookeeper in containers

  `docker exec -it docker_zookeeper_1 zkCli.sh -server localhost:2181`

## Test connection to Hadoop in containers
  `docker exec -it docker_hadoop_1 /usr/local/hadoop/bin/hadoop fs -ls /` 
  `curl http://localhost:50070`
  `curl http://localhost:8088/cluster`
