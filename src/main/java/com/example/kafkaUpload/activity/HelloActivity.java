package com.example.kafkaUpload.activity;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

/**
 * Temporal activity interface for hello world operations.
 * This demonstrates the basic structure for Temporal activities.
 */
@ActivityInterface
public interface HelloActivity {

    /**
     * Simple hello world activity method.
     * 
     * @param name the name to greet
     * @return a greeting message
     */
    @ActivityMethod
    String sayHello(String name);
} 