package com.example.kafkaUpload.workflow;

import com.example.kafkaUpload.activity.ThumbnailActivity;
import com.example.kafkaUpload.activity.VirusScanActivity;
import com.example.kafkaUpload.constants.SearchAttributeConstants;
import com.example.kafkaUpload.model.*;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class FileProcessingWorkflowImpl implements FileProcessingWorkflow {

    private final VirusScanActivity virusScanActivity;
    private final ThumbnailActivity thumbnailActivity;

    public FileProcessingWorkflowImpl() {
        ActivityOptions activityOptions = ActivityOptions.newBuilder()
                .setStartToCloseTimeout(Duration.ofMinutes(5))
                .setRetryOptions(io.temporal.common.RetryOptions.newBuilder()
                        .setInitialInterval(Duration.ofSeconds(1))
                        .setMaximumInterval(Duration.ofSeconds(10))
                        .setMaximumAttempts(3)
                        .build())
                .build();

        this.virusScanActivity = Workflow.newActivityStub(VirusScanActivity.class, activityOptions);
        this.thumbnailActivity = Workflow.newActivityStub(ThumbnailActivity.class, activityOptions);
    }

    @Override
    public ProcessingResult processFile(FileProcessingMessage message) {
        log.info("Starting file processing workflow for file: {}", message.getFilePath());
        
        ProcessingResult result = new ProcessingResult();
        result.setFileId(message.getFileId());
        result.setFilePath(message.getFilePath());
        result.setStatus(ProcessingResult.ProcessingStatus.STARTED);
        result.setStartTime(LocalDateTime.now());
        result.setCompletedSteps(new ArrayList<>());

        ScanResult scanResult = null;

        try {
            // Step 1: Virus Scan (required for all files)
            log.info("Executing virus scan for file: {}", message.getFilePath());
            scanResult = virusScanActivity.scanFile(message.getFilePath());
            
            
            if (!scanResult.isClean()) {
                log.warn("Virus scan failed for file: {} - Status: {}", 
                        message.getFilePath(), scanResult.getStatus());
                result.setStatus(ProcessingResult.ProcessingStatus.FAILED);
                result.setErrorMessage("Virus scan failed: " + scanResult.getStatus());
                return result;
            }
            
            result.getCompletedSteps().add(ProcessingResult.ProcessingStep.VIRUS_SCAN);
            result.setStatus(ProcessingResult.ProcessingStatus.VIRUS_SCAN_COMPLETED);
            log.info("Virus scan completed successfully for file: {}", message.getFilePath());

            // Step 2: Thumbnail Creation (only for image files and after successful virus scan)
            if (message.isImageFile()) {
                log.info("File is an image, creating thumbnail for: {}", message.getFilePath());
                ThumbnailResult thumbnailResult = thumbnailActivity.createThumbnail(message.getFilePath());
                
                if (thumbnailResult.isSuccessful()) {
                    result.getCompletedSteps().add(ProcessingResult.ProcessingStep.THUMBNAIL_CREATION);
                    result.setStatus(ProcessingResult.ProcessingStatus.THUMBNAIL_COMPLETED);
                    log.info("Thumbnail created successfully for file: {} at: {}", 
                            message.getFilePath(), thumbnailResult.getThumbnailPath());
                } else {
                    log.warn("Thumbnail creation failed for file: {} - Status: {}", 
                            message.getFilePath(), thumbnailResult.getStatus());
                    // Continue processing even if thumbnail fails - it's not critical
                }
            } else {
                log.info("File is not an image, skipping thumbnail creation for: {}", message.getFilePath());
            }

            result.setStatus(ProcessingResult.ProcessingStatus.COMPLETED);
            log.info("File processing workflow completed successfully for file: {}", message.getFilePath());

        } catch (Exception e) {
            log.error("File processing workflow failed for file: {}", message.getFilePath(), e);
            result.setStatus(ProcessingResult.ProcessingStatus.FAILED);
            result.setErrorMessage("Processing failed: " + e.getMessage());
        } finally {
            result.setEndTime(LocalDateTime.now());
            if (result.getStartTime() != null) {
                result.setProcessingTimeMs(
                    Duration.between(result.getStartTime(), result.getEndTime()).toMillis()
                );
            }
            
            // Update search attributes once at the end of workflow execution
            updateFinalSearchAttributes(result, scanResult);
        }

        return result;
    }

    /**
     * Updates all search attributes once at the end of workflow execution.
     * This approach minimizes Temporal Cloud costs by reducing the number of search attribute operations.
     * 
     * @param result the processing result containing completed steps and final status
     * @param scanResult the virus scan result (may be null if scan failed to start)
     */
    private void updateFinalSearchAttributes(ProcessingResult result, ScanResult scanResult) {
        try {
            // Convert completed steps to strings for search
            List<String> completedStepsStrings = result.getCompletedSteps()
                .stream()
                .map(Enum::toString)
                .collect(Collectors.toList());
            
            // Determine virus scan status for search attribute
            String virusScanStatus = scanResult != null 
                ? scanResult.getStatus().toString() 
                : "FAILED";
            
            // Update both search attributes in a single call
            Workflow.upsertTypedSearchAttributes(
                SearchAttributeConstants.VIRUS_SCAN_RESULT.valueSet(virusScanStatus),
                SearchAttributeConstants.COMPLETED_STEPS.valueSet(completedStepsStrings)
            );
            
            log.debug("Updated final search attributes - VirusScanResult: {}, CompletedSteps: {}", 
                     virusScanStatus, completedStepsStrings);
        } catch (Exception e) {
            log.warn("Failed to update final search attributes: {}", e.getMessage());
        }
    }
}