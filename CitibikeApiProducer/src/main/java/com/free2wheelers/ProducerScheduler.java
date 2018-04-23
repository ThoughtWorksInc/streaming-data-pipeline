package com.free2wheelers;

import com.free2wheelers.services.ApiProducer;
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
    private ApiProducer apiProducer;

    @Value("${producer.url}")
    private String url;

    @Scheduled(cron="${producer.cron}")
    public void scheduledProducer() {

        RestTemplate template = new RestTemplate();
        HttpEntity<String> response = template.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, String.class);

        apiProducer.sendMessage(response);
    }
}
