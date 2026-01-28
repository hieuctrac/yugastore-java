#!/bin/bash

set -e

echo "🚀 Starting YugaStore locally with Docker..."

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker and try again."
    exit 1
fi

echo "📦 Building and starting all services with Docker Compose..."
echo "   (Maven builds will happen inside Docker containers to avoid local config issues)"

# Start services with Docker Compose (includes building)
docker-compose up --build -d

echo ""
echo "✅ YugaStore is starting up! Services will be available at:"
echo ""
echo "🌐 React UI:          http://localhost:8080"
echo "🚪 API Gateway:       http://localhost:8081"
echo "🔐 Login Service:     http://localhost:8085"
echo "📦 Products Service:  http://localhost:8082"
echo "🛒 Cart Service:      http://localhost:8083"
echo "💳 Checkout Service:  http://localhost:8086"
echo "🔍 Eureka Dashboard:  http://localhost:8761"
echo "🗄️  YugabyteDB UI:     http://localhost:15433"
echo ""
echo "📊 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop: ./stop-local.sh"
echo ""
echo "⏳ Services are starting... This may take several minutes on first run."
echo "   Maven builds happen inside containers, so initial startup will be slower."
echo ""
echo "💡 Check service status with: docker-compose ps"
echo "🔍 View specific service logs: docker-compose logs -f [service-name]"