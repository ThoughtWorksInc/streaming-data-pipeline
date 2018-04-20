package com.free2wheelers;

import com.free2wheelers.services.StationStatusProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ProducerScheduler {

    @Autowired
    private StationStatusProducer stationStatusProducer;

    @Value("${producer.stationStatus.url}")
    private String stationStatusUrl;

    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    public void scheduledProducer() {

        RestTemplate template = new RestTemplate();
        HttpEntity<String> response = template.exchange(stationStatusUrl, HttpMethod.GET, HttpEntity.EMPTY, String.class);

        stationStatusProducer.sendMessage(response);
    }
}
