# Multi-stage build: Build stage
FROM eclipse-temurin:17-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven

# Set working directory
WORKDIR /app

# Copy pom files for dependency resolution
COPY pom.xml ./
COPY cart-microservice/pom.xml ./cart-microservice/

# Copy source code
COPY cart-microservice/src ./cart-microservice/src

# Build the application
RUN mvn clean package -pl cart-microservice -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre

# Copy built JAR from builder stage
COPY --from=builder /app/cart-microservice/target/cart-microservice-0.0.1-SNAPSHOT.jar /app.jar

EXPOSE 8083

# Run with environment variable support
CMD ["java", "-jar", "/app.jar"]