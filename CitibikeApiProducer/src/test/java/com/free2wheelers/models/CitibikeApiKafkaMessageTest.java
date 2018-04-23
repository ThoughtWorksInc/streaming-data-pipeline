package com.free2wheelers.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CitibikeApiKafkaMessageTest {

    @Test
    public void shouldReturnStringWithMetadataAndPayload() {
        String payload = "Test Message";
        MessageMetadata metadata = mock(MessageMetadata.class);
        when(metadata.toString()).thenReturn("Test Metadata");

        CitibikeApiKafkaMessage message = new CitibikeApiKafkaMessage(payload, metadata);

        assertEquals("{\"metadata\": Test Metadata, \"payload\": Test Message}",
                message.getMessageString());
    }
}
