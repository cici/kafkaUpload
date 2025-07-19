package com.example.kafkaUpload.activity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Implementation of the HelloActivity interface.
 * This demonstrates how to implement Temporal activities.
 */
@Slf4j
@Component
public class HelloActivityImpl implements HelloActivity {

    /**
     * Implementation of the sayHello activity method.
     * 
     * @param name the name to greet
     * @return a greeting message
     */
    @Override
    public String sayHello(String name) {
        log.info("Executing sayHello activity for name: {}", name);
        return "Hello, " + name + "! Welcome to the Kafka Upload Service.";
    }
}