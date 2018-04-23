package com.free2wheelers.services;

import com.free2wheelers.models.MessageMetadata;
import com.free2wheelers.models.CitibikeApiKafkaMessage;
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
public class ApiProducer {

    private static Logger logger = LoggerFactory.getLogger(ApiProducer.class);

    @Value("${producer.topic}")
    private String writeTopic;

    @Value("${producer.producerId}")
    private String producerId;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final MetadataGenerator metadataGenerator;

    @Autowired
    public ApiProducer(KafkaTemplate<String, String> kafkaTemplate, MetadataGenerator metadataGenerator) {
        this.kafkaTemplate = kafkaTemplate;
        this.metadataGenerator = metadataGenerator;
    }

    public void sendMessage(final HttpEntity<String> response) {
        String message = response.getBody();
        long contentLength = response.getHeaders().getContentLength();
        String messageId = metadataGenerator.generateUniqueKey();
        long ingestionTime = metadataGenerator.getCurrentTimeMillis();

        MessageMetadata messageMetadata = new MessageMetadata(ingestionTime, producerId, messageId, contentLength);
        String kafkaMessage = new CitibikeApiKafkaMessage(message, messageMetadata).getMessageString();
        ListenableFuture<SendResult<String, String>> future = this.kafkaTemplate.send(writeTopic, messageId, kafkaMessage);
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
