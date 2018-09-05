FROM ryandjf/docker-spark

ADD target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar /app.jar
ENTRYPOINT spark-submit --class com.free2wheelers.apps.StationApp --name StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  /app.jar



