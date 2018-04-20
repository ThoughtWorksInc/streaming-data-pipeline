package com.free2wheelers


import java.util.Properties

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.sql.{DataFrame, ForeachWriter, Row, SparkSession}
import org.scalatest.{FunSpec, Matchers}

class UtilsTest extends FunSpec with Matchers {
  val testSession = SparkSession
    .builder
    .master("local")
    .appName("testSession")
    .getOrCreate()


  describe("FooBarThing") {
    it("should return a dataframe with the contents of the station_status Kafka topic") {

      val serverProps = new Properties()
      serverProps.put("zookeeper.connect", "localhost:2181") // THIS IS A HACK -- need to start a test Zookeeper
      serverProps.put("port", "9099")
//      serverProps.put("replication-factor", "1")
//      serverProps.put("offset.topic.replication.factor", "1")
//      serverProps.put("offsets.topic.num.partitions", "1")
      val kafkaConfig: KafkaConfig = KafkaConfig(serverProps)
      val kafkaServer = new KafkaServerStartable(kafkaConfig)
      kafkaServer.startup()

      val producerProps = new Properties()
      val brokerConfig = "localhost:9099"
      producerProps.put("bootstrap.servers", brokerConfig)
      producerProps.put("client.id", "ScalaProducerExample")
      producerProps.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      producerProps.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

      val producer = new KafkaProducer[String, String](producerProps)

      producer.send(new ProducerRecord("station_status", "KEY", "ABC"))
      producer.send(new ProducerRecord("station_status", "key2","ABC"))
      producer.send(new ProducerRecord("station_status","key3", "ABC"))
      producer.send(new ProducerRecord("station_status", "key4", "ABC"))

      producer.close()

//      val dataFrame = Utils.createDataFrameFromKafkaStationStatusTopic(testSession, brokerConfig)
//      var messagesRead = 0
//
//      dataFrame.writeStream.start()
//
//      messagesRead should be(4)


      kafkaServer.shutdown()
    }
  }

}
