FROM ryandjf/docker-spark

ADD target/scala-2.11/free2wheelers-monitoring_2.11-0.0.1.jar /app.jar
ENTRYPOINT spark-submit --class com.free2wheelers.apps.AccessHDFS --name AccessHDFSApp  /app.jar