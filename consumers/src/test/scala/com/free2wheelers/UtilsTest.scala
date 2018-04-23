package com.free2wheelers

import java.net.ServerSocket
import java.nio.file.{Files, Path}
import java.util.Properties

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.commons.io.FileUtils
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class UtilsTest extends FunSpec with Matchers with BeforeAndAfter with Eventually {
  val testSession = SparkSession
    .builder
    .master("local")
    .appName("testSession")
    .getOrCreate()
  val STATION_STATUS_TOPIC_NAME: String = "station_status"
  var zookeeper: TestingServer = _
  var kafkaServer: KafkaServerStartable = _
  var producer: KafkaProducer[String, String] = _
  var brokerConfig: String = _
  var kafkaTempDirectory: Path = Files.createTempDirectory("tempKafkaDirectory")

  before {
    zookeeper = new TestingServer()
    zookeeper.start()

    val kafkaServerProperties = new Properties()
    val kafkaPort = getAvailablePort
    kafkaServerProperties.put("zookeeper.connect", zookeeper.getConnectString)
    kafkaServerProperties.put("port", kafkaPort.toString)
    kafkaServerProperties.put("log.dir", kafkaTempDirectory.toAbsolutePath.toString)
    kafkaServerProperties.put("advertised.host.name", "localhost")
    kafkaServer = new KafkaServerStartable(KafkaConfig(kafkaServerProperties))
    kafkaServer.startup()

    val producerProps = new Properties()
    brokerConfig = s"localhost:$kafkaPort"
    producerProps.put("bootstrap.servers", brokerConfig)
    producerProps.put("client.id", "ScalaProducerExample")
    producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    producer = new KafkaProducer[String, String](producerProps)
  }

  after {
    producer.close()
    kafkaServer.shutdown()
    zookeeper.stop()

    FileUtils.deleteQuietly(kafkaTempDirectory.toFile)
  }

  describe("Utils#createDataFrameFromKafkaStationStatusTopic") {
    it("should return a dataframe with the contents of the given Kafka topic") {

      val dataFrame = Utils.createDataFrameFromKafkaStationStatusTopic(testSession, brokerConfig)

      val messagesReceivedAccumulator = testSession.sparkContext.longAccumulator

      dataFrame
        .writeStream
        .foreach(new ForeachWriter[Row] {
          override def open(partitionId: Long, version: Long): Boolean = {
            println(s"Opening: $partitionId")

            true
          }

          override def process(value: Row): Unit = {
            println(s"Processing: $value \t Accumulator is ${messagesReceivedAccumulator.value}")
            messagesReceivedAccumulator.add(1)
          }

          override def close(errorOrNull: Throwable): Unit = {
            println(s"Closing")
          }
        })
        .start()

      producer.send(new ProducerRecord(STATION_STATUS_TOPIC_NAME, "KEY0", "ABC"))
      producer.send(new ProducerRecord(STATION_STATUS_TOPIC_NAME, "KEY1", "ABC"))
      producer.send(new ProducerRecord(STATION_STATUS_TOPIC_NAME, "KEY2", "ABC"))
      producer.send(new ProducerRecord(STATION_STATUS_TOPIC_NAME, "KEY3", "ABC"))

      eventually(timeout(Span(30, Seconds))) {
        messagesReceivedAccumulator.value should be(4)
      }
    }
  }

  private def getAvailablePort: Int = {
    val freeSocket = new ServerSocket(0)
    val freePort = freeSocket.getLocalPort
    freeSocket.close()
    freePort
  }

  // Good things
  // - Tests our configuration of our Spark-Karka consumer connection

  // Bad things
  // - We need to spin up a real Kafka, Zookeeper, create a real directory, etc. to test that configuration
  // - This makes the test seem integration-y, when it really isn't written for that reason

  // TODO
  // - For this test, test that we're getting the keys and values correctly (instead of just a count)
  // - For this story (#19), write the messages to HDFS (store in data lake)
}
