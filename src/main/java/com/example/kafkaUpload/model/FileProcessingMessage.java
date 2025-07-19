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
public class FileProcessingMessage {
    private String fileId;
    private String filePath;
    private String fileName;
    private String fileType;
    private long fileSize;
    private LocalDateTime createdAt;
    
    public boolean isImageFile() {
        if (fileType == null) return false;
        return fileType.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|tiff|webp)$");
    }
}