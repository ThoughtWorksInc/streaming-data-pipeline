package com.free2wheelers.services;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MetadataGenerator {

    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    public String generateUniqueKey() {
        return UUID.randomUUID().toString();
    }
}
