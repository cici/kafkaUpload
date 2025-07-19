package com.example.kafkaUpload.activity;

import com.example.kafkaUpload.model.ThumbnailResult;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ThumbnailActivity {
    
    @ActivityMethod
    ThumbnailResult createThumbnail(String filePath);
}