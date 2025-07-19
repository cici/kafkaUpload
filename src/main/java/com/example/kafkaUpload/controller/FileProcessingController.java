package com.example.kafkaUpload.controller;

import com.example.kafkaUpload.model.FileProcessingMessage;
import com.example.kafkaUpload.service.FileProcessingService;
import com.example.kafkaUpload.service.TestDataGeneratorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for file processing operations.
 * Provides endpoints for triggering file processing and generating test data.
 */
@Slf4j
@RestController
@RequestMapping("/api/file-processing")
public class FileProcessingController {

    @Autowired
    private FileProcessingService fileProcessingService;

    @Autowired
    private TestDataGeneratorService testDataGeneratorService;

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "file-processing");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Triggers file processing for a specific file.
     */
    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> processFile(@RequestBody FileProcessingMessage message) {
        log.info("Received file processing request: fileId={}, filePath={}", 
                message.getFileId(), message.getFilePath());

        try {
            fileProcessingService.triggerFileProcessing(message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "accepted");
            response.put("fileId", message.getFileId());
            response.put("filePath", message.getFilePath());
            response.put("message", "File processing request submitted successfully");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Failed to process file processing request", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to submit file processing request: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generates and sends a random file processing message for testing.
     */
    @PostMapping("/test/generate-random")
    public ResponseEntity<Map<String, Object>> generateRandomMessage() {
        log.info("Generating random file processing message");

        try {
            FileProcessingMessage message = testDataGeneratorService.generateRandomFileProcessingMessage();
            
            if (message != null) {
                fileProcessingService.triggerFileProcessing(message);
                
                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("fileId", message.getFileId());
                response.put("filePath", message.getFilePath());
                response.put("fileName", message.getFileName());
                response.put("fileType", message.getFileType());
                response.put("fileSize", message.getFileSize());
                response.put("message", "Random file processing message generated and sent");
                
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Failed to generate random message");
                
                return ResponseEntity.internalServerError().body(response);
            }
            
        } catch (Exception e) {
            log.error("Failed to generate random file processing message", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to generate random message: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Generates and sends a batch of file processing messages for load testing.
     */
    @PostMapping("/test/generate-batch")
    public ResponseEntity<Map<String, Object>> generateBatch(@RequestParam(defaultValue = "10") int batchSize) {
        log.info("Generating batch of {} file processing messages", batchSize);

        try {
            CompletableFuture<Void> future = testDataGeneratorService.generateAndSendBatch(batchSize);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "accepted");
            response.put("batchSize", batchSize);
            response.put("message", "Batch generation started");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("Failed to generate batch", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to generate batch: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Starts continuous generation of file processing messages.
     */
    @PostMapping("/test/start-continuous")
    public ResponseEntity<Map<String, Object>> startContinuousGeneration(
            @RequestParam(defaultValue = "10") int messagesPerSecond) {
        log.info("Starting continuous generation at {} messages per second", messagesPerSecond);

        try {
            testDataGeneratorService.startContinuousGeneration(messagesPerSecond);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "started");
            response.put("messagesPerSecond", messagesPerSecond);
            response.put("message", "Continuous generation started");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to start continuous generation", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to start continuous generation: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Stops continuous generation.
     */
    @PostMapping("/test/stop-continuous")
    public ResponseEntity<Map<String, Object>> stopContinuousGeneration() {
        log.info("Stopping continuous generation");

        try {
            testDataGeneratorService.stopContinuousGeneration();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "stopped");
            response.put("message", "Continuous generation stopped");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to stop continuous generation", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to stop continuous generation: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Regenerates sample files.
     */
    @PostMapping("/test/regenerate-samples")
    public ResponseEntity<Map<String, Object>> regenerateSamples() {
        log.info("Regenerating sample files");

        try {
            testDataGeneratorService.generateSampleFiles();
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Sample files regenerated successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Failed to regenerate sample files", e);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Failed to regenerate sample files: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}