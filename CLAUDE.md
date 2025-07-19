# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application (version 3.2.3) that serves as a Kafka Upload service using Temporal as the workflow engine. The project uses Java 17 and Gradle as the build tool.

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "com.example.kafkaUpload.controller.HelloControllerTest"
```

### Development Workflow
- Application runs on http://localhost:8080
- Health check endpoint: GET /api/health
- Hello endpoint: GET /api/hello
- Actuator health endpoint: GET /actuator/health

## Architecture

### Core Components
- **Main Application**: `KafkaUploadApplication.java` - Spring Boot entry point
- **Controllers**: Handle REST endpoints in `/controller` package
- **Activities**: Temporal activity interfaces and implementations in `/activity` package
- **Configuration**: Spring configuration classes in `/config` package

### Key Technologies
- **Spring Boot 3.2.3**: Main framework
- **Temporal SDK 1.30.1**: Workflow engine (basic structure in place, not fully configured)
- **Lombok**: Reduces boilerplate code
- **Spring Boot Actuator**: Monitoring and metrics

### Package Structure
```
com.example.kafkaUpload/
├── KafkaUploadApplication.java    # Main entry point
├── config/                        # Configuration classes
│   └── TemporalConfig.java        # Temporal configuration (skeleton)
├── controller/                    # REST controllers
│   └── HelloController.java       # Basic endpoints
├── activity/                      # Temporal activities
│   ├── HelloActivity.java         # Activity interface
│   └── HelloActivityImpl.java     # Activity implementation
├── service/                       # Business logic (to be added)
├── repository/                    # Data access (to be added)
└── model/                         # DTOs and entities (to be added)
```

## Configuration

### Application Configuration
- Configuration is in `application.yml`
- Server port: 8080
- Logging configured for application and Temporal components
- Temporal configuration placeholder exists but not fully implemented

### Temporal Integration
- Temporal SDK dependencies are included
- Basic activity structure exists (`HelloActivity`)
- TemporalConfig class exists but needs implementation
- Task queue configured as "kafka-upload-task-queue"

## Development Guidelines

### Code Style
- Follows Spring Boot best practices as defined in `.cursor/rules/springboot.mdc`
- Uses constructor injection for dependencies
- Proper layered architecture (controller → service → repository)
- Lombok for reducing boilerplate code

### Testing
- Uses JUnit 5 platform
- Test classes mirror source structure
- Integration tests use Spring Boot test slices
- Example test: `HelloControllerTest.java`

## API Endpoints

### Core Endpoints
- `GET /api/hello` - Service information and features
- `GET /api/health` - Basic health check
- `GET /api/file-processing/health` - File processing service health

### File Processing
- `POST /api/file-processing/process` - Submit file processing request
- `POST /api/file-processing/test/generate-random` - Generate random test message
- `POST /api/file-processing/test/generate-batch?batchSize=10` - Generate batch of messages
- `POST /api/file-processing/test/start-continuous?messagesPerSecond=10` - Start continuous load testing
- `POST /api/file-processing/test/stop-continuous` - Stop continuous generation
- `POST /api/file-processing/test/regenerate-samples` - Regenerate sample files

## Development Workflow

1. **Start the Application**: `./gradlew bootRun`
2. **Generate Test Data**: `curl -X POST http://localhost:8080/api/file-processing/test/generate-random`
3. **Monitor Processing**: Check logs for workflow execution
4. **Load Testing**: Use batch generation or continuous generation endpoints

## Architecture Overview

The application implements a complete file processing pipeline:

1. **Kafka Consumer** receives file processing messages
2. **Temporal Workflow** orchestrates the processing steps
3. **Virus Scan Activity** simulates file scanning (configurable failure rates)
4. **Thumbnail Activity** creates thumbnails for image files
5. **Results** are published back to Kafka

## Configuration

Key configuration properties:
- `kafka.topic.file-processing`: Input topic for file processing requests
- `kafka.topic.processing-results`: Output topic for processing results
- `temporal.task-queue`: Temporal task queue name
- `file-processing.virus-scan.simulation.failure-rate`: Virus scan failure rate (0.0-1.0)
- `file-processing.thumbnail.simulation.failure-rate`: Thumbnail creation failure rate (0.0-1.0)

## Testing

The application includes comprehensive test data generation:
- Sample files are created in `./test-data/` directory
- Various file types supported (images, documents, etc.)
- Configurable failure simulation for reliability testing
- Load testing capabilities with continuous message generation

## Important Notes

- Uses **embedded Kafka** for proof of concept (configured in KafkaConfig)
- **Temporal server** must be running locally on port 7233
- All file processing is **simulated** with configurable delays and failure rates
- **Test data** is automatically generated on startup
- The project follows Spring Boot 3.x conventions with Temporal integration