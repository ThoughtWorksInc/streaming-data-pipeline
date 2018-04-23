package com.free2wheelers

import java.net.ServerSocket
import java.util.Properties

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.curator.test.TestingServer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.{ForeachWriter, Row, SparkSession}
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Minutes, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers}

class UtilsTest extends FunSpec with Matchers with BeforeAndAfter with Eventually {
  val testSession = SparkSession
    .builder
    .master("local")
    .appName("testSession")
    .getOrCreate()
  var zookeeper: TestingServer = _
  var kafkaServer: KafkaServerStartable = _
  var producer: KafkaProducer[String, String] = _
  var brokerConfig: String = _

  before {
    zookeeper = new TestingServer()
    zookeeper.start()

    val kafkaServerProperties = new Properties()
    val kafkaPort = getAvailablePort
    kafkaServerProperties.put("zookeeper.connect", zookeeper.getConnectString)
    kafkaServerProperties.put("port", kafkaPort.toString)
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
  }

  describe("FooBarThing") {
    it("should return a dataframe with the contents of the station_status Kafka topic") {

      val dataFrame = Utils.createDataFrameFromKafkaStationStatusTopic(testSession, brokerConfig)
      var messagesRead = 0

      dataFrame.printSchema()

      dataFrame
        .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
        .writeStream
        .foreach(new ForeachWriter[Row] {
          override def open(partitionId: Long, version: Long): Boolean = {
            println(s"Opening: $partitionId")

            true
          }

          override def process(value: Row): Unit = {
            println(s"Processing: $value")
            messagesRead += 1
          }

          override def close(errorOrNull: Throwable): Unit = {
            println(s"Closing")
          }
        })
        .trigger(Trigger.ProcessingTime("1 second"))
        .start()

      producer.send(new ProducerRecord("station_status", "KEY", "ABC"))
      producer.send(new ProducerRecord("station_status", "key2", "ABC"))
      producer.send(new ProducerRecord("station_status", "key3", "ABC"))
      producer.send(new ProducerRecord("station_status", "key4", "ABC"))

      eventually(timeout(Span(5, Minutes))) {
        messagesRead should be(4)
      }
    }
  }

  private def getAvailablePort: Int = {
    val freeSocket = new ServerSocket(0)
    val freePort = freeSocket.getLocalPort
    freeSocket.close()
    freePort
  }
}
