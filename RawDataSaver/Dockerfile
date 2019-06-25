FROM ryandjf/docker-spark
ADD target/scala-2.11/free2wheelers-raw-data-saver_2.11-0.0.1.jar /app.jar
ARG application_name
ARG zookeeper_origin_folder
ENTRYPOINT spark-submit --class com.free2wheelers.apps.StationLocationApp --name ${application_name} --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  /app.jar "zookeeper:2181" "${zookeeper_origin_folder}"
