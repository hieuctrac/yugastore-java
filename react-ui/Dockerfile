# Multi-stage build: Build stage
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml ./
COPY react-ui/pom.xml ./react-ui/

# Copy source code
COPY react-ui/src ./react-ui/src

# Build the application
RUN mvn clean package -pl react-ui -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Copy built JAR from builder stage
COPY --from=builder /app/react-ui/target/react-ui-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8080

# Run with environment variable support
CMD ["java", "-jar", "/app.jar"]