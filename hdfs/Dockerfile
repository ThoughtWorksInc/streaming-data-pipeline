FROM sequenceiq/hadoop-docker:2.7.0
COPY seed.sh /seed.sh
ENV hdfs_server "hadoop:9000"
ENV hadoop_path "/usr/local/hadoop/bin/hadoop"
CMD ["/seed.sh"]
