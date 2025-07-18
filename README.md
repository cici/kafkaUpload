# Kafka Upload Service

A Spring Boot application that uses Temporal as the workflow engine for managing file upload workflows.

## Project Structure

This project follows Spring Boot best practices with a layered architecture:

```
src/
├── main/
│   ├── java/
│   │   └── com/example/kafkaUpload/
│   │       ├── KafkaUploadApplication.java    (Main entry point)
│   │       ├── config/                        (Configuration classes)
│   │       ├── controller/                    (REST controllers)
│   │       ├── activity/                      (Temporal activity code)
│   │       ├── service/                       (Business logic services)
│   │       ├── repository/                    (Data access repositories)
│   │       ├── model/                         (Data transfer objects)
│   │       ├── exception/                     (Custom exceptions)
│   │       └── util/                          (Utility classes)
│   └── resources/
│       └── application.yml                    (Application configuration)
└── test/
    └── java/
        └── com/example/kafkaUpload/
            ├── KafkaUploadApplicationTests.java
            └── controller/
                └── HelloControllerTest.java
```

## Prerequisites

- Java 17 or higher
- Gradle 8.5 or higher
- Temporal server (for future workflow execution)

## Getting Started

### 1. Build the Project

```bash
./gradlew build
```

### 2. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 3. Test the Application

```bash
# Run all tests
./gradlew test

# Run the application and test endpoints
curl http://localhost:8080/api/hello
curl http://localhost:8080/api/health
```

## Available Endpoints

- `GET /api/hello` - Returns a hello world message
- `GET /api/health` - Returns application health status
- `GET /actuator/health` - Spring Boot actuator health endpoint

## Temporal Integration

This project includes Temporal Java SDK dependencies and basic activity structure:

- **Temporal SDK**: Version 1.22.3
- **Activity Interface**: `HelloActivity` with `sayHello` method
- **Activity Implementation**: `HelloActivityImpl` with basic logging

### Next Steps for Temporal

1. **Configure Temporal Client**: Add Temporal client configuration in `TemporalConfig`
2. **Create Workflows**: Implement workflow interfaces and implementations
3. **Set up Workers**: Configure Temporal workers to process activities
4. **Add Workflow Execution**: Create services to start and manage workflows

## Configuration

The application configuration is in `src/main/resources/application.yml`:

- **Server Port**: 8080
- **Application Name**: kafka-upload-service
- **Logging**: Configured for application and Temporal logs
- **Actuator**: Health and info endpoints enabled

## Development

### Adding New Features

1. **Controllers**: Add REST endpoints in the `controller` package
2. **Services**: Add business logic in the `service` package
3. **Activities**: Add Temporal activities in the `activity` package
4. **Configuration**: Add configuration classes in the `config` package

### Testing

- **Unit Tests**: Use `@WebMvcTest` for controller tests
- **Integration Tests**: Use `@SpringBootTest` for full application tests
- **Temporal Tests**: Use `temporal-testing` dependency for workflow testing

## Dependencies

### Core Dependencies
- **Spring Boot**: 3.2.3
- **Spring Boot Web**: REST API support
- **Spring Boot Actuator**: Monitoring and metrics
- **Lombok**: Reduces boilerplate code

### Temporal Dependencies
- **Temporal SDK**: Core Temporal functionality (v1.22.4)
- **Temporal Testing**: Testing utilities for workflows (v1.22.4)

## Contributing

1. Follow the established package structure
2. Add appropriate tests for new functionality
3. Use constructor injection for dependencies
4. Follow Spring Boot best practices
5. Add proper documentation and comments

## License

This project is licensed under the Apache License 2.0. 