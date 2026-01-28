# Multi-stage build: Build stage
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy pom file and source code (build context is api-gateway-microservice)
COPY pom.xml ./
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Copy built JAR from builder stage
COPY --from=builder /app/target/api-gateway-microservice-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8081

# Run with environment variable support
CMD ["java", "-jar", "/app.jar"]