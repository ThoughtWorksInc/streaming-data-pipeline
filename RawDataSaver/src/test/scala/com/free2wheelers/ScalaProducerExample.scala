package com.free2wheelers

import java.util.{Date, Properties}

import kafka.server.{KafkaConfig, KafkaServerStartable}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

import scala.util.Random

object ScalaProducerExample {

  def main(args: Array[String]): Unit = {
    val serverProps = new Properties()
    serverProps.put("zookeeper.connect", "localhost:2181")
    serverProps.put("port", "9093")
    val kafkaConfig: KafkaConfig = KafkaConfig(serverProps)
    val kafkaServer = new KafkaServerStartable(kafkaConfig)
    kafkaServer.startup()

    Thread.sleep(1000)

    val events = 2
    val topic = "foo"
    val brokers = "localhost:9093"
    val rnd = new Random()
    val props = new Properties()
    props.put("bootstrap.servers", brokers)
    props.put("client.id", "ScalaProducerExample")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    val t = System.currentTimeMillis()
    for (nEvents <- Range(0, events)) {
      val runtime = new Date().getTime()
      val ip = "192.168.2." + rnd.nextInt(255)
      val msg = runtime + "," + nEvents + ",www.example.com," + ip
      val data = new ProducerRecord[String, String](topic, ip, msg)

      producer.send(data)
    }

    System.out.println("sent per second: " + events * 1000 / (System.currentTimeMillis() - t))
    producer.close()

    kafkaServer.shutdown()
  }
}
