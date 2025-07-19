package com.example.kafkaUpload.activity;

import com.example.kafkaUpload.model.ScanResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface VirusScanActivity {
    
    @ActivityMethod
    ScanResult scanFile(String filePath);
}