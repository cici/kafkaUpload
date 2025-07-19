package com.example.kafkaUpload.kafka;

import com.example.kafkaUpload.model.FileProcessingMessage;
import com.example.kafkaUpload.model.ProcessingResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for file processing messages and results.
 * Sends file processing requests and publishes processing results.
 */
@Slf4j
@Component
public class FileProcessingProducer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.file-processing:file-processing-requests}")
    private String fileProcessingTopic;

    @Value("${kafka.topic.processing-results:processing-results}")
    private String processingResultsTopic;

    /**
     * Sends a file processing request to Kafka.
     * 
     * @param message the file processing message
     * @return CompletableFuture for the send result
     */
    public CompletableFuture<SendResult<String, Object>> sendFileProcessingRequest(FileProcessingMessage message) {
        log.info("Sending file processing request: fileId={}, filePath={}", 
                message.getFileId(), message.getFilePath());

        return kafkaTemplate.send(fileProcessingTopic, message.getFileId(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send file processing request: fileId={}, error={}", 
                                message.getFileId(), ex.getMessage(), ex);
                    } else {
                        log.info("Successfully sent file processing request: fileId={}, topic={}, partition={}, offset={}", 
                                message.getFileId(), 
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Publishes a processing result to Kafka.
     * 
     * @param result the processing result
     * @return CompletableFuture for the send result
     */
    public CompletableFuture<SendResult<String, Object>> publishProcessingResult(ProcessingResult result) {
        log.info("Publishing processing result: fileId={}, status={}", 
                result.getFileId(), result.getStatus());

        return kafkaTemplate.send(processingResultsTopic, result.getFileId(), result)
                .whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish processing result: fileId={}, error={}", 
                                result.getFileId(), ex.getMessage(), ex);
                    } else {
                        log.info("Successfully published processing result: fileId={}, status={}, topic={}, partition={}, offset={}", 
                                result.getFileId(), 
                                result.getStatus(),
                                sendResult.getRecordMetadata().topic(),
                                sendResult.getRecordMetadata().partition(),
                                sendResult.getRecordMetadata().offset());
                    }
                });
    }

    /**
     * Sends a message to a custom topic (for testing purposes).
     * 
     * @param topic the topic name
     * @param key the message key
     * @param message the message payload
     * @return CompletableFuture for the send result
     */
    public CompletableFuture<SendResult<String, Object>> sendMessage(String topic, String key, Object message) {
        log.debug("Sending message to topic: {}, key: {}", topic, key);

        return kafkaTemplate.send(topic, key, message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to send message to topic: {}, key: {}, error: {}", 
                                topic, key, ex.getMessage(), ex);
                    } else {
                        log.debug("Successfully sent message to topic: {}, key: {}, partition: {}, offset: {}", 
                                topic, key, 
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}