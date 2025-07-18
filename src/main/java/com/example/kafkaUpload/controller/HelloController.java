package com.example.kafkaUpload.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple REST controller for hello world functionality.
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
    public String hello() {
        return "Hello World from Kafka Upload Service!";
    }

    /**
     * Health check endpoint.
     * 
     * @return application status
     */
    @GetMapping("/health")
    public String health() {
        return "Application is running successfully!";
    }
} 