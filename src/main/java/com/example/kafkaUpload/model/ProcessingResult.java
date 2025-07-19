package com.example.kafkaUpload.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessingResult {
    private String fileId;
    private String filePath;
    private ProcessingStatus status;
    private List<ProcessingStep> completedSteps = new ArrayList<>();
    private String errorMessage;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long processingTimeMs;
    
    public enum ProcessingStatus {
        STARTED,
        VIRUS_SCAN_COMPLETED,
        THUMBNAIL_COMPLETED,
        COMPLETED,
        FAILED
    }
    
    public enum ProcessingStep {
        VIRUS_SCAN,
        THUMBNAIL_CREATION
    }
}