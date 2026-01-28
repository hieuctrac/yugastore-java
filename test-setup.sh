#!/bin/bash

echo "🧪 Testing Docker setup..."

# Test Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "✅ Docker is running"

# Test building just the database and eureka service first
echo "🗄️  Starting YugabyteDB..."
docker-compose up -d yugabytedb

echo "⏳ Waiting for database to be healthy..."
sleep 10

echo "📊 Building and starting Eureka server..."
docker-compose up --build -d eureka-server

echo "✅ Basic services are starting!"
echo ""
echo "Check status with: docker-compose ps"
echo "View logs with: docker-compose logs -f"
echo "Stop with: docker-compose down"