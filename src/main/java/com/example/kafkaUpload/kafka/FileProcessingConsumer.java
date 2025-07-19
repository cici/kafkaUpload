package com.example.kafkaUpload.kafka;

import com.example.kafkaUpload.model.FileProcessingMessage;
import com.example.kafkaUpload.service.FileProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for file processing messages.
 * Listens to file processing requests and triggers Temporal workflows.
 */
@Slf4j
@Component
public class FileProcessingConsumer {

    @Autowired
    private FileProcessingService fileProcessingService;

    @KafkaListener(
        topics = "${kafka.topic.file-processing:file-processing-requests}",
        groupId = "${kafka.consumer.group-id:file-processors}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeFileProcessingMessage(
            @Payload FileProcessingMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        log.info("Received file processing message: fileId={}, filePath={}, topic={}, partition={}, offset={}", 
                message.getFileId(), message.getFilePath(), topic, partition, offset);

        try {
            // Start Temporal workflow for file processing
            fileProcessingService.startFileProcessingWorkflow(message);
            
            // Acknowledge message processing
            acknowledgment.acknowledge();
            
            log.info("Successfully processed file processing message: fileId={}", message.getFileId());
            
        } catch (Exception e) {
            log.error("Error processing file processing message: fileId={}, error={}", 
                    message.getFileId(), e.getMessage(), e);
            
            // TODO: Implement dead letter queue or retry logic
            // For now, we'll acknowledge to prevent reprocessing
            acknowledgment.acknowledge();
        }
    }
}