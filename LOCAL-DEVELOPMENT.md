# YugaStore Local Development with Docker

This guide helps you run the entire YugaStore application locally using Docker.

## Prerequisites

- Docker Desktop installed and running
- Java 17+ (for building the applications)
- Maven 3.6+ (for building the applications)

## Quick Start

### 1. Start Everything
```bash
./run-local.sh
```

This script will:
- Build all Java microservices with Maven
- Start YugabyteDB database
- Initialize database schemas
- Start all microservices with Docker Compose

### 2. Access the Application

Once started, access these services:

| Service | URL | Description |
|---------|-----|-------------|
| **React UI** | http://localhost:8080 | Main application interface |
| **API Gateway** | http://localhost:8081 | Gateway for all API calls |
| **Login Service** | http://localhost:8085 | Authentication & user management |
| **Products Service** | http://localhost:8082 | Product catalog |
| **Cart Service** | http://localhost:8083 | Shopping cart management |
| **Checkout Service** | http://localhost:8086 | Order processing |
| **Eureka Dashboard** | http://localhost:8761 | Service discovery console |
| **YugabyteDB UI** | http://localhost:15433 | Database administration |

### 3. Stop Everything
```bash
./stop-local.sh
```

## Development Workflow

### Monitor Service Logs
```bash
# View all logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f products-service
docker-compose logs -f login-service
```

### Check Service Status
```bash
docker-compose ps
```

### Restart a Single Service
```bash
# After code changes
mvn clean package -pl login-microservice -DskipTests
docker-compose up --build -d login-service
```

### Database Access

**PostgreSQL API (YSQL) - Port 5433:**
```bash
# Connect via psql
docker exec -it yugastore-db /home/yugabyte/bin/ysqlsh -h localhost -p 5433 -U postgres

# Or from host machine (if you have psql installed)
psql -h localhost -p 5433 -U postgres -d postgres
```

**Cassandra API (YCQL) - Port 9042:**
```bash
# Connect via cqlsh
docker exec -it yugastore-db /home/yugabyte/bin/ycqlsh localhost

# List keyspaces
USE cronos;
DESCRIBE TABLES;
```

## Architecture Overview

```
┌─────────────────┐    ┌─────────────────┐
│   React UI      │    │  API Gateway    │
│   :8080         │◄──►│   :8081         │
└─────────────────┘    └─────────────────┘
                                │
              ┌─────────────────┼─────────────────┐
              │                 │                 │
    ┌─────────▼───┐   ┌────────▼────┐   ┌────────▼────┐
    │ Login Svc   │   │Products Svc │   │  Cart Svc   │
    │   :8085     │   │   :8082     │   │   :8083     │
    └─────────────┘   └─────────────┘   └─────────────┘
              │                 │                 │
              │         ┌───────▼────┐           │
              │         │Checkout Svc│           │
              │         │   :8086    │           │
              │         └────────────┘           │
              │                                  │
              └────────────┬─────────────────────┘
                           │
                  ┌────────▼────────┐
                  │   YugabyteDB    │
                  │ YSQL:5433       │
                  │ YCQL:9042       │
                  └─────────────────┘
```

## Database Schema

### Current State (step-0a branch)
- **YSQL (PostgreSQL API)**: Shopping cart tables
- **YCQL (Cassandra API)**: Product catalog data

### When RBAC is Added (001-rbac-implementation branch)
Additional YSQL tables will include:
- `users` - User accounts and profiles
- `roles` - RBAC role definitions
- `user_roles` - User-role assignments
- `user_sessions` - JWT session tracking
- `audit_logs` - Security audit trail

## Troubleshooting

### Services Won't Start
1. Check if ports are available:
   ```bash
   lsof -i :8080 -i :8081 -i :8082 -i :8083 -i :8085 -i :8086 -i :8761 -i :5433 -i :9042
   ```
2. Check Docker logs:
   ```bash
   docker-compose logs [service-name]
   ```

### Database Connection Issues
1. Ensure YugabyteDB is healthy:
   ```bash
   docker exec yugastore-db bin/yugabyted status
   ```
2. Check database logs:
   ```bash
   docker-compose logs yugabytedb
   ```

### Build Issues
1. Clean and rebuild:
   ```bash
   mvn clean package -DskipTests
   ```
2. Check Java version:
   ```bash
   java --version  # Should be 17+
   ```

### Clean Reset
To completely reset the environment:
```bash
./stop-local.sh
docker-compose down -v --rmi all
./run-local.sh
```

## Configuration Notes

- **Database passwords**: Currently set to empty string for local development
- **Eureka discovery**: Services register with Eureka for inter-service communication
- **Network isolation**: All services run in `yugastore-network` for secure communication
- **Data persistence**: YugabyteDB data persists in `yugastore_data` Docker volume

## Adding the RBAC System

When switching to the `001-rbac-implementation` branch:

1. The `auth-schema.sql` file will be available
2. Update docker-compose db-init to use `auth-schema.sql` instead of `schema.sql`
3. The login-service will have full RBAC functionality

This provides a solid foundation for local development that can evolve as features are added!