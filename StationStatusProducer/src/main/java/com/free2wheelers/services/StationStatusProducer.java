package com.free2wheelers.services;

import com.free2wheelers.models.MessageMetadata;
import com.free2wheelers.models.StationStatusKafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class StationStatusProducer {

    private static Logger logger = LoggerFactory.getLogger(StationStatusProducer.class);

    @Value("${producer.stationStatus.topic}")
    private String writeTopic;

    @Value("${producer.stationStatus.producerId}")
    private String stationStatusProducerId;

    @Autowired
    private KafkaTemplate<String, String> stationStatusTemplate;

    @Autowired
    private MetadataGenerator metadataGenerator;


    public void sendMessage(final HttpEntity<String> response) {
        String message = response.getBody();
        long contentLength = response.getHeaders().getContentLength();
        String messageId = metadataGenerator.generateUniqueKey();
        long ingestionTime = metadataGenerator.getCurrentTimeMillis();

        MessageMetadata messageMetadata = new MessageMetadata(ingestionTime, stationStatusProducerId, messageId, contentLength);
        String kafkaMessage = new StationStatusKafkaMessage(message, messageMetadata).getMessageString();
        ListenableFuture<SendResult<String, String>> future = this.stationStatusTemplate.send(writeTopic, messageId, kafkaMessage);
        future.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {

            @Override
            public void onSuccess(SendResult<String, String> result) {
                logger.info("Success sending message");
            }

            @Override
            public void onFailure(Throwable ex) {
                logger.error("Failure to send message");
            }

        });
    }

}
