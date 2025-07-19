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
public class ScanResult {
    private String fileId;
    private ScanStatus status;
    private String virusName;
    private String scanEngine;
    private LocalDateTime scanTime;
    private long scanDurationMs;
    private String checksum;
    
    public enum ScanStatus {
        CLEAN,
        INFECTED,
        CORRUPTED,
        SCAN_FAILED
    }
    
    public boolean isClean() {
        return status == ScanStatus.CLEAN;
    }
}