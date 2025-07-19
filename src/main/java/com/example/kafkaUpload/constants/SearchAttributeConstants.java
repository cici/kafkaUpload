package com.example.kafkaUpload.constants;

import io.temporal.common.SearchAttributeKey;

import java.util.List;

/**
 * Constants for Temporal Search Attributes used in file processing workflows.
 * These search attributes enable filtering and querying workflows by processing status and results.
 */
public class SearchAttributeConstants {
    
    /**
     * Search attribute for virus scan result status.
     * Values: CLEAN, INFECTED, CORRUPTED, SCAN_FAILED
     */
    public static final SearchAttributeKey<String> VIRUS_SCAN_RESULT = 
        SearchAttributeKey.forText("VirusScanResult");
    
    /**
     * Search attribute for completed processing steps.
     * Values: VIRUS_SCAN, THUMBNAIL_CREATION
     */
    public static final SearchAttributeKey<List<String>> COMPLETED_STEPS = 
        SearchAttributeKey.forKeywordList("CompletedSteps");
    
    // Private constructor to prevent instantiation
    private SearchAttributeConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}