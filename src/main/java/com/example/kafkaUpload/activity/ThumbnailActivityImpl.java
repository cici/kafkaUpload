package com.example.kafkaUpload.activity;

import com.example.kafkaUpload.model.ThumbnailResult;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of the ThumbnailActivity interface.
 * This provides stubbed thumbnail creation functionality with configurable failure simulation.
 */
@Slf4j
@Component
public class ThumbnailActivityImpl implements ThumbnailActivity {

    @Value("${file-processing.thumbnail.simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${file-processing.thumbnail.simulation.failure-rate:0.05}")
    private double failureRate;

    @Value("${file-processing.thumbnail.simulation.processing-time-ms:300}")
    private long processingTimeMs;

    @Value("${file-processing.thumbnail.max-width:200}")
    private int maxWidth;

    @Value("${file-processing.thumbnail.max-height:200}")
    private int maxHeight;

    @Value("${file-processing.thumbnail.format:jpg}")
    private String thumbnailFormat;

    @Value("${file-processing.thumbnails-directory:./thumbnails}")
    private String thumbnailsDirectory;

    private final Random random = new Random();

    @Override
    public ThumbnailResult createThumbnail(String filePath) {
        log.info("Starting thumbnail creation for file: {}", filePath);
        
        long startTime = System.currentTimeMillis();
        ThumbnailResult result = new ThumbnailResult();
        result.setFileId(UUID.randomUUID().toString());
        result.setOriginalFilePath(filePath);
        result.setCreationTime(LocalDateTime.now());

        try {
            // Verify file exists
            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                throw ApplicationFailure.newFailure(
                    "File not found: " + filePath, 
                    "FILE_NOT_FOUND"
                );
            }

            // Check if it's an image file
            if (!isImageFile(filePath)) {
                result.setStatus(ThumbnailResult.ThumbnailStatus.SKIPPED_NOT_IMAGE);
                log.info("File is not an image, skipping thumbnail creation: {}", filePath);
                return result;
            }

            // Create thumbnails directory if it doesn't exist
            Path thumbnailDir = Paths.get(thumbnailsDirectory);
            if (!Files.exists(thumbnailDir)) {
                Files.createDirectories(thumbnailDir);
            }

            // Generate thumbnail path
            String fileName = file.getFileName().toString();
            String nameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
            String thumbnailPath = thumbnailDir.resolve(nameWithoutExtension + "_thumb." + thumbnailFormat).toString();
            result.setThumbnailPath(thumbnailPath);

            // Simulate processing time
            if (simulationEnabled) {
                simulateProcessingTime();
            }

            // Simulate thumbnail creation
            if (simulationEnabled && random.nextDouble() < failureRate) {
                result.setStatus(ThumbnailResult.ThumbnailStatus.FAILED);
                result.setErrorMessage("Simulated thumbnail creation failure");
                log.warn("Thumbnail creation failed for file: {}", filePath);
            } else {
                // Simulate successful thumbnail creation
                createSimulatedThumbnail(thumbnailPath);
                
                result.setStatus(ThumbnailResult.ThumbnailStatus.CREATED);
                result.setThumbnailWidth(maxWidth);
                result.setThumbnailHeight(maxHeight);
                result.setThumbnailSize(calculateSimulatedSize());
                result.setThumbnailFormat(thumbnailFormat.toUpperCase());
                
                log.info("Thumbnail created successfully for file: {} at: {}", filePath, thumbnailPath);
            }

        } catch (Exception e) {
            log.error("Error during thumbnail creation for file: {}", filePath, e);
            result.setStatus(ThumbnailResult.ThumbnailStatus.FAILED);
            result.setErrorMessage("Thumbnail creation failed: " + e.getMessage());
            throw ApplicationFailure.newFailure(
                "Thumbnail creation failed: " + e.getMessage(),
                "THUMBNAIL_ERROR"
            );
        } finally {
            long endTime = System.currentTimeMillis();
            result.setProcessingTimeMs(endTime - startTime);
        }

        return result;
    }

    private boolean isImageFile(String filePath) {
        String fileName = filePath.toLowerCase();
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || 
               fileName.endsWith(".png") || fileName.endsWith(".gif") || 
               fileName.endsWith(".bmp") || fileName.endsWith(".tiff") || 
               fileName.endsWith(".webp");
    }

    private void simulateProcessingTime() {
        try {
            // Add some randomness to processing time (±50%)
            long variance = (long) (processingTimeMs * 0.5 * random.nextGaussian());
            long actualProcessingTime = Math.max(50, processingTimeMs + variance);
            Thread.sleep(actualProcessingTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ApplicationFailure.newFailure(
                "Thumbnail creation interrupted",
                "THUMBNAIL_INTERRUPTED"
            );
        }
    }

    private void createSimulatedThumbnail(String thumbnailPath) {
        try {
            // Create a simple text file to simulate thumbnail creation
            String simulatedContent = "Simulated thumbnail created at " + LocalDateTime.now() + 
                                    "\nOriginal dimensions: " + (maxWidth * 2) + "x" + (maxHeight * 2) +
                                    "\nThumbnail dimensions: " + maxWidth + "x" + maxHeight;
            Files.write(Paths.get(thumbnailPath), simulatedContent.getBytes());
        } catch (IOException e) {
            log.warn("Failed to create simulated thumbnail file: {}", thumbnailPath, e);
        }
    }

    private long calculateSimulatedSize() {
        // Simulate thumbnail size based on format and dimensions
        double compressionRatio = "jpg".equals(thumbnailFormat) ? 0.8 : 0.9;
        return (long) (maxWidth * maxHeight * 3 * compressionRatio); // 3 bytes per pixel (RGB)
    }
}