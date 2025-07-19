package com.example.kafkaUpload.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ThumbnailResult {
    private String fileId;
    private String originalFilePath;
    private String thumbnailPath;
    private ThumbnailStatus status;
    private int thumbnailWidth;
    private int thumbnailHeight;
    private long thumbnailSize;
    private String thumbnailFormat;
    private LocalDateTime creationTime;
    private long processingTimeMs;
    private String errorMessage;
    
    public enum ThumbnailStatus {
        CREATED,
        FAILED,
        SKIPPED_NOT_IMAGE,
        SKIPPED_UNSUPPORTED_FORMAT
    }
    
    public boolean isSuccessful() {
        return status == ThumbnailStatus.CREATED;
    }
}