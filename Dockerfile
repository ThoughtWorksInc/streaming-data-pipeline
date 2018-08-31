FROM openjdk:8u181-jre-stretch


RUN	apt-get update &&  \
	apt-get install -y curl tar apt-transport-https

RUN \
	echo "deb https://dl.bintray.com/sbt/debian /" | tee -a /etc/apt/sources.list.d/sbt.list && \
	apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 2EE0EA64E40A89B84B2DF73499E82A75642AC823 && \
	apt-get update

RUN apt-get install -y sbt

RUN \
  curl -fsL https://archive.apache.org/dist/spark/spark-2.3.0/spark-2.3.0-bin-hadoop2.7.tgz | tar xfz - -C /root/

RUN \
  curl -fsL http://mirrors.hust.edu.cn/apache/kafka/2.0.0/kafka_2.11-2.0.0.tgz | tar xfz - -C /root/

RUN cp /root/kafka_2.11-2.0.0/config/zookeeper.properties /root/ && \
	cp /root/kafka_2.11-2.0.0/config/server.properties /root/kafka.properties

RUN apt-get install -y procps

WORKDIR /root

EXPOSE 2181
EXPOSE 18080
EXPOSE 8080

ENV PATH "$PATH:/root/spark-2.3.0-bin-hadoop2.7/sbin:/root/spark-2.3.0-bin-hadoop2.7/bin:/root/kafka_2.11-2.0.0/bin"

COPY setup.sh /root/

CMD ["/bin/bash", "/root/setup.sh"]