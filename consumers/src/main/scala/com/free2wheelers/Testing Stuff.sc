import java.util.Properties

import kafka.server.{KafkaConfig, KafkaServerStartable}

val serverProps = new Properties()
serverProps.put("zookeeper.connect", "localhost:2181") // THIS IS A HACK -- need to start a test Zookeeper
serverProps.put("port", "9099")
val kafkaConfig: KafkaConfig = KafkaConfig(serverProps)
val kafkaServer = new KafkaServerStartable(kafkaConfig)
kafkaServer.startup()