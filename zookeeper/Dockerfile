FROM zookeeper:3.3.6
COPY seed.sh /seed.sh
ENV hdfs_server "hadoop:9000"
ENV kafka_server "kafka:9092"
ENV zk_command "zkCli.sh -server zookeeper:2181"
CMD ["/seed.sh"]
