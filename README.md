# File Processing System - Complete Setup Guide

## Overview
A complete file processing system with Kafka messaging and Temporal workflows for virus scanning and thumbnail generation.

## Prerequisites
- Java 17+
- Docker & Docker Compose
- Temporal CLI (`brew install temporal`)

## Complete Setup Instructions

### Step 1: Start Kafka
```bash
# Start Kafka and Zookeeper
docker-compose up -d

# Verify Kafka is running
docker-compose ps
```

### Step 2: Start Temporal Server
```bash
# Start Temporal development server (in a separate terminal)
temporal server start-dev

# You should see:
# - Temporal server running on localhost:7233
# - Temporal Web UI on http://localhost:8233
```

### Step 3: Start the Application
```bash
# Start the Spring Boot application with full Temporal support
SPRING_PROFILES_ACTIVE=full ./gradlew bootRun

# Look for these log messages indicating the worker started:
# "Starting Temporal worker on task queue: file-processing-queue"
# "Started KafkaUploadApplication"
```

### Step 4: Test the Complete Pipeline

#### Basic Health Check
```bash
curl http://localhost:8080/api/hello
```

#### Testing Options

**Single File Processing**
```bash
# Generate a single random file processing request
curl -X POST http://localhost:8080/api/file-processing/test/generate-random
```

**Batch Processing**
```bash
# Generate a batch of 10 requests
curl -X POST "http://localhost:8080/api/file-processing/test/generate-batch?batchSize=10"

# Generate a larger batch of 50 requests
curl -X POST "http://localhost:8080/api/file-processing/test/generate-batch?batchSize=50"

# Generate 100 requests for stress testing
curl -X POST "http://localhost:8080/api/file-processing/test/generate-batch?batchSize=100"
```

**Continuous Load Testing**
```bash
# Low load: 10 messages per second
curl -X POST "http://localhost:8080/api/file-processing/test/start-continuous?messagesPerSecond=10"

# Medium load: 50 messages per second
curl -X POST "http://localhost:8080/api/file-processing/test/start-continuous?messagesPerSecond=50"

# High load: 100 messages per second
curl -X POST "http://localhost:8080/api/file-processing/test/start-continuous?messagesPerSecond=100"

# Stop continuous processing
curl -X POST http://localhost:8080/api/file-processing/test/stop-continuous
```

**Regenerate Test Data**
```bash
# Regenerate sample files if needed
curl -X POST http://localhost:8080/api/file-processing/test/regenerate-samples
```

#### Monitor Workflow Execution
1. **Check Application Logs** - You should see:
   ```
   Received file processing message: fileId=xxx, filePath=xxx
   Starting file processing workflow for fileId: xxx
   Starting virus scan for file: xxx
   Virus scan completed successfully for file: xxx
   File is an image, creating thumbnail for: xxx (if image)
   Thumbnail created successfully for file: xxx
   ```

2. **Check Temporal Web UI** - Open http://localhost:8233
   - Navigate to "Workflows"
   - You should see `file-processing-{fileId}` workflows
   - Click on a workflow to see execution details

#### Advanced Testing

**Performance Testing**
```bash
# Test different load levels to find system limits
curl -X POST "http://localhost:8080/api/file-processing/test/start-continuous?messagesPerSecond=200"
curl -X POST "http://localhost:8080/api/file-processing/test/start-continuous?messagesPerSecond=500"

# Always stop continuous processing when done
curl -X POST http://localhost:8080/api/file-processing/test/stop-continuous
```

**Monitor System Performance**
- Watch application logs for processing times
- Check Temporal Web UI for workflow completion rates  
- Monitor Kafka lag and throughput
- Observe CPU and memory usage during load tests

## What You Should See

### 1. Kafka Messages
- Messages published to `file-processing-requests` topic
- Results published to `processing-results` topic

### 2. Temporal Workflows
- Workflows visible in Temporal Web UI
- Sequential execution: Virus Scan → Thumbnail Creation
- Retry policies for failed activities

### 3. File Processing
- Test files created in `./test-data/` directory
- Thumbnails created in `./thumbnails/` directory
- Configurable failure simulation (10% virus scan, 5% thumbnail)

### 4. Application Logs
```
2025-07-19 11:xx:xx - Starting Temporal worker on task queue: file-processing-queue
2025-07-19 11:xx:xx - [Consumer] Subscribed to topic(s): file-processing-requests
2025-07-19 11:xx:xx - file-processors: partitions assigned: [file-processing-requests-0, 1, 2]
2025-07-19 11:xx:xx - Received file processing message: fileId=abc-123
2025-07-19 11:xx:xx - Starting file processing workflow for fileId: abc-123
2025-07-19 11:xx:xx - Starting virus scan for file: ./test-data/sample_image.jpg
2025-07-19 11:xx:xx - Virus scan completed successfully for file: ./test-data/sample_image.jpg
2025-07-19 11:xx:xx - File is an image, creating thumbnail for: ./test-data/sample_image.jpg
2025-07-19 11:xx:xx - Thumbnail created successfully for file: ./test-data/sample_image.jpg
```

## Architecture Components

### Kafka Configuration
- **Topics**: `file-processing-requests`, `processing-results`
- **Partitions**: 3 (for parallel processing)
- **Consumer Group**: `file-processors`

### Temporal Configuration
- **Namespace**: `default`
- **Task Queue**: `file-processing-queue`
- **Workflow**: `FileProcessingWorkflow`
- **Activities**: `VirusScanActivity`, `ThumbnailActivity`

### File Processing Flow
1. **Kafka Message** → Consumer receives file processing request
2. **Workflow Start** → Temporal workflow initiated
3. **Virus Scan** → File scanned for viruses (simulated)
4. **Conditional Thumbnail** → If clean + image file, create thumbnail
5. **Result Publishing** → Results sent back to Kafka

## Performance Characteristics
- **Target**: 5,000 RPS, 5M documents/day
- **Scalability**: Multiple consumer threads, Temporal worker pools
- **Reliability**: Temporal retry policies, Kafka durability
- **Monitoring**: Actuator endpoints, Temporal Web UI

## Troubleshooting

### Temporal Worker Not Starting
1. Ensure Temporal server is running: `temporal server start-dev`
2. Check application profile: `SPRING_PROFILES_ACTIVE=full`
3. Verify logs show: "Starting Temporal worker on task queue"

### Kafka Connection Issues
1. Ensure Docker containers are running: `docker-compose ps`
2. Check Kafka logs: `docker-compose logs kafka`

### No Workflows Executing
1. Verify worker registration in logs
2. Check Temporal Web UI for workflow instances
3. Ensure test data generation is working

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/hello` | GET | Service information |
| `/api/file-processing/health` | GET | Processing service health |
| `/api/file-processing/test/generate-random` | POST | Generate single test message |
| `/api/file-processing/test/generate-batch?batchSize=N` | POST | Generate N test messages |
| `/api/file-processing/test/start-continuous?messagesPerSecond=N` | POST | Start continuous load testing |
| `/api/file-processing/test/stop-continuous` | POST | Stop continuous testing |

## Success Criteria
✅ Kafka consumers connected and processing messages  
✅ Temporal worker registered and executing workflows  
✅ Virus scan activity completing (with simulated results)  
✅ Thumbnail creation for image files  
✅ Sequential processing (scan → thumbnail)  
✅ Results published back to Kafka  
✅ Workflows visible in Temporal Web UI  
✅ Configurable failure simulation working  
✅ Load testing capabilities functional  