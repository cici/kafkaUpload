package com.example.kafkaUpload.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple REST controller for basic application functionality.
 * This serves as a basic health check and demonstrates the application is running.
 */
@RestController
@RequestMapping("/api")
public class HelloController {

    /**
     * Simple hello world endpoint.
     * 
     * @return a greeting message
     */
    @GetMapping("/hello")
    public Map<String, Object> hello() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello World from Kafka Upload Service!");
        response.put("service", "kafka-upload-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("features", new String[]{
            "Kafka Message Processing",
            "Temporal Workflow Engine",
            "File Processing Pipeline",
            "Virus Scanning Simulation",
            "Thumbnail Generation"
        });
        return response;
    }

    /**
     * Health check endpoint.
     * 
     * @return application status
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "kafka-upload-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("message", "Application is running successfully!");
        return response;
    }
} 