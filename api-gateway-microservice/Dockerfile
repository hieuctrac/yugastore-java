# Multi-stage build: Build stage
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml ./
COPY api-gateway-microservice/pom.xml ./api-gateway-microservice/

# Copy source code
COPY api-gateway-microservice/src ./api-gateway-microservice/src

# Build the application
RUN mvn clean package -pl api-gateway-microservice -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Copy built JAR from builder stage
COPY --from=builder /app/api-gateway-microservice/target/api-gateway-microservice-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8081

# Run with environment variable support
CMD ["java", "-jar", "/app.jar"]