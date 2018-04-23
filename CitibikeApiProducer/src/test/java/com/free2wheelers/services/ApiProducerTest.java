package com.free2wheelers.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
public class ApiProducerTest {

    @InjectMocks
    private ApiProducer apiProducer;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private MetadataGenerator metadataGenerator;

    @Mock
    private ListenableFuture<SendResult<String, String>> future;

    @Value("${producer.topic}")
    private String testWriteTopic;

    @Value("${producer.producerId}")
    private String testProducerId;

    @Test
    public void shouldAddMetadataAndSendMessage() throws NoSuchFieldException, IllegalAccessException {

        //cannot find another way to inject in the test values...
        Field writeTopic = ApiProducer.class.getDeclaredField("writeTopic");
        writeTopic.setAccessible(true);
        writeTopic.set(apiProducer, testWriteTopic);

        Field producerId = ApiProducer.class.getDeclaredField("producerId");
        producerId.setAccessible(true);
        producerId.set(apiProducer, testProducerId);

        HttpEntity<String> response = mock(HttpEntity.class, Answers.RETURNS_DEEP_STUBS);
        when(response.getBody()).thenReturn("LargeJsonMessage");
        when(response.getHeaders().getContentLength()).thenReturn(1234L);
        when(metadataGenerator.generateUniqueKey()).thenReturn("123e4567-e89b-12d3-a456-426655440001");
        when(metadataGenerator.getCurrentTimeMillis()).thenReturn(1524237281590L);
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);

        apiProducer.sendMessage(response);
        verify(kafkaTemplate).send(
                "test_station_status",
                "123e4567-e89b-12d3-a456-426655440001",
                "{\"metadata\": {\"producer_id\": \"test_station_status_producer\", " +
                        "\"size\": 1234, " +
                        "\"message_id\": \"123e4567-e89b-12d3-a456-426655440001\", " +
                        "\"ingestion_time\": 1524237281590}, " +
                        "\"payload\": LargeJsonMessage}"
        );

    }
}
