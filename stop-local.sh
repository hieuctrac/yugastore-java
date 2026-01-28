#!/bin/bash

echo "🛑 Stopping YugaStore local environment..."

# Stop and remove containers
docker-compose down

echo "🧹 Cleaning up..."
echo ""
echo "✅ YugaStore stopped successfully!"
echo ""
echo "💡 To remove all data (including database): docker-compose down -v"
echo "🐳 To remove built images: docker-compose down --rmi all"