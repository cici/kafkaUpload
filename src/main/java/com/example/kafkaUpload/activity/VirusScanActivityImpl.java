package com.example.kafkaUpload.activity;

import com.example.kafkaUpload.model.ScanResult;
import io.temporal.failure.ApplicationFailure;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

/**
 * Implementation of the VirusScanActivity interface.
 * This provides stubbed virus scanning functionality with configurable failure simulation.
 */
@Slf4j
@Component
public class VirusScanActivityImpl implements VirusScanActivity {

    @Value("${file-processing.virus-scan.simulation.enabled:true}")
    private boolean simulationEnabled;

    @Value("${file-processing.virus-scan.simulation.failure-rate:0.1}")
    private double failureRate;

    @Value("${file-processing.virus-scan.simulation.processing-time-ms:500}")
    private long processingTimeMs;

    private final Random random = new Random();

    @Override
    public ScanResult scanFile(String filePath) {
        log.info("Starting virus scan for file: {}", filePath);
        
        long startTime = System.currentTimeMillis();
        ScanResult result = new ScanResult();
        result.setFileId(UUID.randomUUID().toString());
        result.setScanTime(LocalDateTime.now());
        result.setScanEngine("SimulatedAV v1.0");

        try {
            // Verify file exists
            Path file = Paths.get(filePath);
            if (!Files.exists(file)) {
                throw ApplicationFailure.newFailure(
                    "File not found: " + filePath, 
                    "FILE_NOT_FOUND"
                );
            }

            // Calculate file checksum
            result.setChecksum(calculateChecksum(file));

            // Simulate processing time
            if (simulationEnabled) {
                simulateProcessingTime();
            }

            // Simulate scan results
            if (simulationEnabled && random.nextDouble() < failureRate) {
                // Simulate various failure scenarios
                double failureType = random.nextDouble();
                if (failureType < 0.3) {
                    result.setStatus(ScanResult.ScanStatus.INFECTED);
                    result.setVirusName("Trojan.Generic.Simulated");
                } else if (failureType < 0.6) {
                    result.setStatus(ScanResult.ScanStatus.CORRUPTED);
                } else {
                    result.setStatus(ScanResult.ScanStatus.SCAN_FAILED);
                }
                log.warn("Virus scan failed for file: {} - Status: {}", filePath, result.getStatus());
            } else {
                result.setStatus(ScanResult.ScanStatus.CLEAN);
                log.info("Virus scan completed successfully for file: {}", filePath);
            }

        } catch (Exception e) {
            log.error("Error during virus scan for file: {}", filePath, e);
            result.setStatus(ScanResult.ScanStatus.SCAN_FAILED);
            throw ApplicationFailure.newFailure(
                "Virus scan failed: " + e.getMessage(),
                "SCAN_ERROR"
            );
        } finally {
            long endTime = System.currentTimeMillis();
            result.setScanDurationMs(endTime - startTime);
        }

        return result;
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
                "Virus scan interrupted",
                "SCAN_INTERRUPTED"
            );
        }
    }

    private String calculateChecksum(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(file);
            byte[] digest = md.digest(fileBytes);
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("Failed to calculate checksum for file: {}", file, e);
            return "checksum_unavailable";
        }
    }
}