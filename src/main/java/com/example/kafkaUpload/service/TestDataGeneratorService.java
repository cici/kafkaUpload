package com.example.kafkaUpload.service;

import com.example.kafkaUpload.model.FileProcessingMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for generating test data and sample files.
 * Creates sample files and generates file processing messages for testing.
 */
@Slf4j
@Service
public class TestDataGeneratorService {

    @Autowired
    private FileProcessingService fileProcessingService;

    @Value("${file-processing.test-data-directory:./test-data}")
    private String testDataDirectory;

    private final Random random = new Random();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    // Sample file types for testing
    private final List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp");
    private final List<String> documentExtensions = Arrays.asList("pdf", "doc", "docx", "txt", "xlsx", "pptx");
    private final List<String> allExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "tiff", "webp", 
                                                           "pdf", "doc", "docx", "txt", "xlsx", "pptx", "zip", "mp4", "avi");

    @PostConstruct
    public void init() {
        try {
            createTestDataDirectory();
            generateSampleFiles();
        } catch (Exception e) {
            log.error("Failed to initialize test data generator", e);
        }
    }

    /**
     * Creates the test data directory if it doesn't exist.
     */
    private void createTestDataDirectory() throws IOException {
        Path testDir = Paths.get(testDataDirectory);
        if (!Files.exists(testDir)) {
            Files.createDirectories(testDir);
            log.info("Created test data directory: {}", testDir.toAbsolutePath());
        }
    }

    /**
     * Generates sample files for testing.
     */
    public void generateSampleFiles() {
        log.info("Generating sample files in directory: {}", testDataDirectory);

        try {
            // Generate sample image files
            for (String ext : imageExtensions) {
                createSampleFile("sample_image." + ext, "This is a sample " + ext.toUpperCase() + " image file for testing.", 1024 * 50); // 50KB
            }

            // Generate sample document files
            for (String ext : documentExtensions) {
                createSampleFile("sample_document." + ext, "This is a sample " + ext.toUpperCase() + " document file for testing.", 1024 * 100); // 100KB
            }

            // Generate some larger files
            createSampleFile("large_image.jpg", "This is a large image file for testing.", 1024 * 1024 * 2); // 2MB
            createSampleFile("large_document.pdf", "This is a large document file for testing.", 1024 * 1024 * 3); // 3MB

            log.info("Sample files generated successfully");

        } catch (Exception e) {
            log.error("Failed to generate sample files", e);
        }
    }

    /**
     * Creates a sample file with the specified content and size.
     */
    private void createSampleFile(String fileName, String baseContent, int targetSize) throws IOException {
        Path filePath = Paths.get(testDataDirectory, fileName);
        
        // Create content by repeating base content to reach target size
        StringBuilder content = new StringBuilder();
        while (content.length() < targetSize) {
            content.append(baseContent).append("\n");
            content.append("Created at: ").append(LocalDateTime.now()).append("\n");
            content.append("File size target: ").append(targetSize).append(" bytes\n");
            content.append("Random data: ").append(UUID.randomUUID().toString()).append("\n");
            content.append("Line number: ").append(content.length() / baseContent.length()).append("\n\n");
        }

        Files.write(filePath, content.toString().getBytes());
        log.debug("Created sample file: {} (size: {} bytes)", fileName, Files.size(filePath));
    }

    /**
     * Generates a random file processing message using existing sample files.
     */
    public FileProcessingMessage generateRandomFileProcessingMessage() {
        try {
            // Get list of sample files
            List<Path> sampleFiles = Files.list(Paths.get(testDataDirectory))
                    .filter(Files::isRegularFile)
                    .toList();

            if (sampleFiles.isEmpty()) {
                generateSampleFiles();
                sampleFiles = Files.list(Paths.get(testDataDirectory))
                        .filter(Files::isRegularFile)
                        .toList();
            }

            // Select a random file
            Path selectedFile = sampleFiles.get(random.nextInt(sampleFiles.size()));
            
            return createFileProcessingMessage(selectedFile);

        } catch (Exception e) {
            log.error("Failed to generate random file processing message", e);
            return null;
        }
    }

    /**
     * Creates a file processing message for the specified file.
     */
    private FileProcessingMessage createFileProcessingMessage(Path filePath) throws IOException {
        FileProcessingMessage message = new FileProcessingMessage();
        message.setFileId(UUID.randomUUID().toString());
        message.setFilePath(filePath.toAbsolutePath().toString());
        message.setFileName(filePath.getFileName().toString());
        message.setFileType(getFileExtension(filePath.getFileName().toString()));
        message.setFileSize(Files.size(filePath));
        message.setCreatedAt(LocalDateTime.now());
        
        return message;
    }

    /**
     * Gets the file extension from a filename.
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(dotIndex + 1) : "";
    }

    /**
     * Generates and sends a batch of file processing messages.
     */
    public CompletableFuture<Void> generateAndSendBatch(int batchSize) {
        log.info("Generating and sending batch of {} file processing messages", batchSize);

        return CompletableFuture.runAsync(() -> {
            for (int i = 0; i < batchSize; i++) {
                try {
                    FileProcessingMessage message = generateRandomFileProcessingMessage();
                    if (message != null) {
                        fileProcessingService.triggerFileProcessing(message);
                        
                        // Add small delay to avoid overwhelming the system
                        if (i < batchSize - 1) {
                            Thread.sleep(100); // 100ms delay between messages
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to generate/send file processing message {}", i, e);
                }
            }
        });
    }

    /**
     * Starts continuous generation of file processing messages for load testing.
     */
    public void startContinuousGeneration(int messagesPerSecond) {
        log.info("Starting continuous generation at {} messages per second", messagesPerSecond);

        long delayMs = 1000 / messagesPerSecond;
        
        scheduler.scheduleAtFixedRate(() -> {
            try {
                FileProcessingMessage message = generateRandomFileProcessingMessage();
                if (message != null) {
                    fileProcessingService.triggerFileProcessing(message);
                }
            } catch (Exception e) {
                log.error("Error in continuous generation", e);
            }
        }, 0, delayMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops continuous generation.
     */
    public void stopContinuousGeneration() {
        log.info("Stopping continuous generation");
        scheduler.shutdown();
    }
}