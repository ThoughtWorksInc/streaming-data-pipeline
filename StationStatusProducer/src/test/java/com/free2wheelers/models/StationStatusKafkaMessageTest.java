package com.free2wheelers.models;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StationStatusKafkaMessageTest {

    @Test
    public void shouldReturnStringWithMetadataAndPayload() {
        String payload = "Test Message";
        MessageMetadata metadata = mock(MessageMetadata.class);
        when(metadata.toString()).thenReturn("Test Metadata");

        StationStatusKafkaMessage message = new StationStatusKafkaMessage(payload, metadata);

        assertEquals("{\"metadata\": Test Metadata, \"payload\": Test Message}",
                message.getMessageString());
    }
}
