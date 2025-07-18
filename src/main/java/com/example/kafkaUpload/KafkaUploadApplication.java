package com.example.kafkaUpload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for Kafka Upload service.
 * This application uses Temporal as the workflow engine for managing
 * file upload workflows.
 */
@SpringBootApplication
public class KafkaUploadApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaUploadApplication.class, args);
    }
} 