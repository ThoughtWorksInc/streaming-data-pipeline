package com.free2wheelers;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
@EnableScheduling
public class Application {

    private static Logger logger = LoggerFactory.getLogger(Application.class);
    private final CountDownLatch latch = new CountDownLatch(100);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args).close();
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            logger.info("Starting app");
            latch.await();
        };
    }
}
