# Local set up
  1. Make sure you have sbt installed, if not, do `brew install sbt`
  2. If you use IntelliJ: make sure you have sbt plugin and Scala plugin installed on IntelliJ
  3. Import project in IntelliJ from sbt
  4. Mark directories as sources / resources / tests as necessary

# Run the consumer
1. Package the jar: in the StationConsumer directory run `sbt package`
2. Run the StationConsumer: `spark-submit --class com.free2wheelers.apps.StationApp --packages org.apache.spark:spark-sql-kafka-0-10_2.11:2.3.0  target/scala-2.11/free2wheelers-station-consumer_2.11-0.0.1.jar`