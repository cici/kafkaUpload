package com.example.kafkaUpload.workflow;

import com.example.kafkaUpload.model.FileProcessingMessage;
import com.example.kafkaUpload.model.ProcessingResult;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface FileProcessingWorkflow {
    
    @WorkflowMethod
    ProcessingResult processFile(FileProcessingMessage message);
}