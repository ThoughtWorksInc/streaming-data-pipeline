package com.free2wheelers.models;

public class StationStatusKafkaMessage {

    private String message;
    private MessageMetadata metadata;

    public StationStatusKafkaMessage(String message,
                                     MessageMetadata metadata) {
        this.message = message;
        this.metadata = metadata;
    }

    public String getMessageString() {
        return "{\"metadata\": " + metadata.toString() + ", \"payload\": " + message + "}";
    }
}
