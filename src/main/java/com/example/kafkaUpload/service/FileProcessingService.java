package com.example.kafkaUpload.service;

import com.example.kafkaUpload.constants.SearchAttributeConstants;
import com.example.kafkaUpload.kafka.FileProcessingProducer;
import com.example.kafkaUpload.model.FileProcessingMessage;
import com.example.kafkaUpload.model.ProcessingResult;
import com.example.kafkaUpload.workflow.FileProcessingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.SearchAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing file processing workflows.
 * Integrates Kafka message consumption with Temporal workflow execution.
 */
@Slf4j
@Service
public class FileProcessingService {

    @Autowired(required = false)
    private WorkflowClient workflowClient;

    @Autowired
    private FileProcessingProducer fileProcessingProducer;

    @Value("${temporal.task-queue:file-processing-queue}")
    private String taskQueue;

    /**
     * Starts a Temporal workflow for file processing.
     * 
     * @param message the file processing message
     * @return the processing result
     */
    public CompletableFuture<ProcessingResult> startFileProcessingWorkflow(FileProcessingMessage message) {
        log.info("Starting file processing workflow for fileId: {}, filePath: {}", 
                message.getFileId(), message.getFilePath());

        if (workflowClient == null) {
            log.warn("Temporal WorkflowClient not available. Skipping workflow execution for fileId: {}", message.getFileId());
            ProcessingResult result = new ProcessingResult();
            result.setFileId(message.getFileId());
            result.setFilePath(message.getFilePath());
            result.setStatus(ProcessingResult.ProcessingStatus.FAILED);
            result.setErrorMessage("Temporal workflow engine not available");
            return CompletableFuture.completedFuture(result);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create initial search attributes
                SearchAttributes searchAttributes = null;
                try {
                    searchAttributes = SearchAttributes.newBuilder()
                            .set(SearchAttributeConstants.VIRUS_SCAN_RESULT, "PENDING")
                            .set(SearchAttributeConstants.COMPLETED_STEPS, new ArrayList<String>())
                            .build();
                    log.debug("Created initial search attributes for workflow: fileId={}", message.getFileId());
                } catch (Exception e) {
                    log.warn("Failed to create initial search attributes: {}", e.getMessage());
                }
                
                // Create workflow options
                WorkflowOptions.Builder optionsBuilder = WorkflowOptions.newBuilder()
                        .setTaskQueue(taskQueue)
                        .setWorkflowId("file-processing-" + message.getFileId())
                        .setWorkflowExecutionTimeout(Duration.ofMinutes(10))
                        .setWorkflowTaskTimeout(Duration.ofMinutes(1));
                
                if (searchAttributes != null) {
                    optionsBuilder.setTypedSearchAttributes(searchAttributes);
                }
                
                WorkflowOptions options = optionsBuilder.build();

                // Create workflow stub
                FileProcessingWorkflow workflow = workflowClient.newWorkflowStub(
                        FileProcessingWorkflow.class, options);

                // Start workflow execution
                ProcessingResult result = workflow.processFile(message);

                log.info("File processing workflow completed for fileId: {}, status: {}", 
                        message.getFileId(), result.getStatus());

                // Publish result to Kafka
                fileProcessingProducer.publishProcessingResult(result);

                return result;

            } catch (Exception e) {
                log.error("Error in file processing workflow for fileId: {}, error: {}", 
                        message.getFileId(), e.getMessage(), e);

                // Create error result
                ProcessingResult errorResult = new ProcessingResult();
                errorResult.setFileId(message.getFileId());
                errorResult.setFilePath(message.getFilePath());
                errorResult.setStatus(ProcessingResult.ProcessingStatus.FAILED);
                errorResult.setErrorMessage("Workflow execution failed: " + e.getMessage());

                // Publish error result
                fileProcessingProducer.publishProcessingResult(errorResult);

                return errorResult;
            }
        });
    }

    /**
     * Triggers a file processing request by sending a message to Kafka.
     * This method is useful for testing or external API calls.
     * 
     * @param message the file processing message
     * @return CompletableFuture for the send result
     */
    public CompletableFuture<Void> triggerFileProcessing(FileProcessingMessage message) {
        log.info("Triggering file processing for fileId: {}, filePath: {}", 
                message.getFileId(), message.getFilePath());

        return fileProcessingProducer.sendFileProcessingRequest(message)
                .thenAccept(result -> {
                    log.info("File processing request sent successfully for fileId: {}", 
                            message.getFileId());
                })
                .exceptionally(ex -> {
                    log.error("Failed to send file processing request for fileId: {}, error: {}", 
                            message.getFileId(), ex.getMessage(), ex);
                    return null;
                });
    }
}