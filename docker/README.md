# Local environment setup

0. Verify that you don't have Zookeeper or Kafka running locally (ports 9092 and 2181 are free)
0. Verify that your Docker Daemon is running
0. Run the following commands:
    ```
    cd docker
    ./docker-compose.sh up -d
    ```
    This  will start 2 containers, one for Kafka and one for Zookeeper. From your Mac, you'll be able to access these on localhost ports 9092 (Kafka) and 2181 (Zookeeper)

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
  
  `curl http://localhost:50070`
  `curl http://localhost:8088/cluster`
