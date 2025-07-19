package com.example.kafkaUpload.config;

import com.example.kafkaUpload.activity.ThumbnailActivityImpl;
import com.example.kafkaUpload.activity.VirusScanActivityImpl;
import com.example.kafkaUpload.workflow.FileProcessingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;


/**
 * Configuration class for Temporal workflow engine.
 * Sets up Temporal client, worker, and workflow registration.
 */
@Slf4j
@Configuration
@Profile("!kafka-only")
public class TemporalConfig {

    @Value("${temporal.target:127.0.0.1:7233}")
    private String temporalTarget;

    @Value("${temporal.namespace:default}")
    private String temporalNamespace;

    @Value("${temporal.task-queue:file-processing-queue}")
    private String taskQueue;

    private WorkerFactory workerFactory;
    private Worker worker;
    private WorkflowClient workflowClient;

    @Bean
    public WorkflowServiceStubs workflowServiceStubs() {
        try {
            return WorkflowServiceStubs.newServiceStubs(
                WorkflowServiceStubsOptions.newBuilder()
                    .setTarget(temporalTarget)
                    .build()
            );
        } catch (Exception e) {
            log.warn("Failed to connect to Temporal server at {}: {}. Some features may not work.", temporalTarget, e.getMessage());
            throw e; // Re-throw to let Spring handle it
        }
    }

    @Bean
    public WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs) {
        this.workflowClient = WorkflowClient.newInstance(
            workflowServiceStubs,
            WorkflowClientOptions.newBuilder()
                .setNamespace(temporalNamespace)
                .build()
        );
        return this.workflowClient;
    }

    @Bean
    public WorkerFactory workerFactory(WorkflowClient workflowClient) {
        this.workerFactory = WorkerFactory.newInstance(workflowClient);
        return this.workerFactory;
    }

    @Bean
    public Worker worker(WorkerFactory workerFactory, 
                        VirusScanActivityImpl virusScanActivity,
                        ThumbnailActivityImpl thumbnailActivity) {
        log.info("Creating Temporal worker for task queue: {}", taskQueue);
        Worker worker = workerFactory.newWorker(taskQueue);
        
        // Register workflow implementations
        worker.registerWorkflowImplementationTypes(FileProcessingWorkflowImpl.class);
        log.info("Registered workflow implementation: {}", FileProcessingWorkflowImpl.class.getSimpleName());
        
        // Register activity implementations
        worker.registerActivitiesImplementations(virusScanActivity, thumbnailActivity);
        log.info("Registered activity implementations: {}, {}", 
                virusScanActivity.getClass().getSimpleName(), 
                thumbnailActivity.getClass().getSimpleName());
        
        // Store references for lifecycle management
        this.workerFactory = workerFactory;
        this.worker = worker;
        
        return worker;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWorker() {
        log.info("ApplicationReadyEvent received - attempting to start Temporal worker");
        
        // First, try to register search attributes
        registerSearchAttributes();
        
        if (workerFactory != null && worker != null) {
            log.info("Starting Temporal worker on task queue: {}", taskQueue);
            try {
                workerFactory.start();
                log.info("✅ Temporal worker started successfully!");
                log.info("Worker is listening for workflows and activities on task queue: {}", taskQueue);
                log.info("Temporal Web UI available at: http://localhost:8233");
            } catch (Exception e) {
                log.error("❌ Failed to start Temporal worker: {}", e.getMessage(), e);
            }
        } else {
            log.error("❌ Cannot start worker - WorkerFactory: {}, Worker: {}", 
                     workerFactory != null ? "✅" : "❌", 
                     worker != null ? "✅" : "❌");
        }
    }

    /**
     * Registers custom search attributes with the Temporal server.
     * For the development server, search attributes should be created manually using:
     * temporal operator search-attribute create --name VirusScanResult --type Text
     * temporal operator search-attribute create --name CompletedSteps --type KeywordList
     */
    private void registerSearchAttributes() {
        log.info("📋 Search Attributes Configuration:");
        log.info("Custom search attributes need to be created manually in development:");
        log.info("  temporal operator search-attribute create --name VirusScanResult --type Text");
        log.info("  temporal operator search-attribute create --name CompletedSteps --type KeywordList");
        log.info("Search attributes will be used automatically in workflows once created.");
    }

    @PreDestroy
    public void stopWorker() {
        if (workerFactory != null) {
            log.info("Stopping Temporal worker");
            workerFactory.shutdown();
        }
    }
} 