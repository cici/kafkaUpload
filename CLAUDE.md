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

## Next Steps for Development

When extending this application:

1. **Temporal Setup**: Complete the TemporalConfig implementation
2. **Service Layer**: Add business logic services
3. **Repository Layer**: Add data access repositories
4. **Models**: Add DTOs and entities for data transfer
5. **Workflow Implementation**: Create Temporal workflow interfaces and implementations
6. **Kafka Integration**: Add Kafka producer/consumer components

## Important Notes

- The application currently has basic REST endpoints but Temporal workflows are not yet configured
- Database configuration is not yet implemented
- Kafka integration is planned but not yet implemented
- The project follows Spring Boot 3.x conventions