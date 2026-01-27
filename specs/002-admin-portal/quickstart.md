# Quick Start: Admin Portal for Product Management

**Feature**: 002-admin-portal
**Date**: 2026-01-27
**Audience**: Developers setting up the admin portal locally

## Overview

This guide helps you build, run, and test the Admin Portal locally. The admin portal consists of two components:
1. **admin-microservice**: Spring Boot backend (port 8084)
2. **admin-ui**: React frontend integrated into react-ui (port 3000)

---

## Prerequisites

Ensure you have the following installed:

- **Java 17** - `java -version`
- **Maven 3.6+** - `mvn -version`
- **Node.js 14+** and **npm** - `node -version && npm -version`
- **YugabyteDB** - `yugabyted version`
- **Git** - `git --version`

---

## Step 1: Clone and Setup Repository

```bash
# If you haven't already cloned the repo
git clone https://github.com/YugabyteDB-Samples/yugastore-java.git
cd yugastore-java

# Checkout the admin portal feature branch
git checkout 002-admin-portal
```

---

## Step 2: Start YugabyteDB

```bash
# Start YugabyteDB
yugabyted start

# Verify it's running
curl http://localhost:15433
# Should return: {"version":"..."}

# You should see both APIs available:
# - YCQL (Cassandra): localhost:9042
# - YSQL (PostgreSQL): localhost:5433
```

---

## Step 3: Initialize Database Schemas

### 3.1 Create YSQL Schema (Admin Tables)

```bash
# Navigate to admin-microservice resources
cd admin-microservice/src/main/resources

# Run the admin schema script
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -f schema-admin.sql

# Verify tables created
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -c "\dt"
# Should show: admin_users, product_audit_log
```

### 3.2 Extend YCQL Schema (Products Table)

```bash
# Navigate to resources directory
cd ../../../resources

# Apply products schema extensions
cqlsh localhost 9042 -f schema-products-v2.cql

# Verify columns added
cqlsh -e "DESCRIBE TABLE products;"
# Should show: is_active, deactivated_at, deactivated_by, version
```

### 3.3 Load Sample Data (Optional)

```bash
# Load existing product data
./dataload.sh

# This populates products table with sample books
# Takes about 2-3 minutes
```

### 3.4 Create Initial Admin User

```bash
# Create default admin user
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte <<EOF
INSERT INTO admin_users (username, password_hash, role, email)
VALUES (
  'admin',
  '\$2a\$10\$N.zmdr9k7uOCQnTEua/5fOQ7Z4K6e3zGKvCAe0zDLFlgrEaP6Fkg2',  -- password: Admin123!
  'ADMIN',
  'admin@yugastore.com'
);

INSERT INTO admin_users (username, password_hash, role, email)
VALUES (
  'editor',
  '\$2a\$10\$N.zmdr9k7uOCQnTEua/5fOQ7Z4K6e3zGKvCAe0zDLFlgrEaP6Fkg2',  -- password: Admin123!
  'EDITOR',
  'editor@yugastore.com'
);

INSERT INTO admin_users (username, password_hash, role, email)
VALUES (
  'viewer',
  '\$2a\$10\$N.zmdr9k7uOCQnTEua/5fOQ7Z4K6e3zGKvCAe0zDLFlgrEaP6Fkg2',  -- password: Admin123!
  'VIEWER',
  'viewer@yugastore.com'
);
EOF

# Verify users created
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -c "SELECT username, role FROM admin_users;"
```

**Default Login Credentials**:
- Username: `admin` / Password: `Admin123!` (Admin role)
- Username: `editor` / Password: `Admin123!` (Editor role)
- Username: `viewer` / Password: `Admin123!` (Viewer role)

---

## Step 4: Start Microservices

You'll need multiple terminal windows. Start services in this order:

### Terminal 1: Eureka Server (Service Discovery)

```bash
cd eureka-server-local
mvn spring-boot:run

# Wait for: "Started YugastoreEurekaServer"
# Verify: http://localhost:8761
```

### Terminal 2: Products Microservice

```bash
cd products-microservice
mvn spring-boot:run

# Wait for: "Started YugastoreProducts"
# Verify: http://localhost:8081/products
```

### Terminal 3: Admin Microservice (NEW)

```bash
cd admin-microservice
mvn clean package -DskipTests
mvn spring-boot:run

# Wait for: "Started YugastoreAdmin"
# Verify: http://localhost:8084/actuator/health
# Should return: {"status":"UP"}
```

### Terminal 4: API Gateway (Optional for production-like setup)

```bash
cd api-gateway-microservice
mvn spring-boot:run

# Wait for: "Started YugastoreApiGateway"
# Verify: http://localhost:8080
```

---

## Step 5: Start React UI (with Admin Portal)

### Terminal 5: React Frontend

```bash
cd react-ui/frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start

# Wait for: "Compiled successfully!"
# Opens browser automatically at http://localhost:3000
```

---

## Step 6: Access the Admin Portal

### Login to Admin Portal

1. Open browser: **http://localhost:3000/admin/login**
2. Enter credentials:
   - Username: `admin`
   - Password: `Admin123!`
3. Click **Login**

You should be redirected to **http://localhost:3000/admin/products**

### Admin Portal Routes

- `/admin/login` - Login page
- `/admin/products` - Product list with search and filters
- `/admin/products/new` - Create new product
- `/admin/products/:asin` - Edit product
- `/admin/audit` - Audit log viewer

---

## Step 7: Verify Functionality

### Test 1: View Products

1. Navigate to `/admin/products`
2. Verify product list displays with pagination
3. Try searching by product name
4. Try filtering by category
5. Verify sort by clicking column headers

### Test 2: Create Product

1. Click "Add Product" button
2. Fill in form:
   - ASIN: `TEST001`
   - Title: `Test Product`
   - Category: `Fiction`
   - Author: `Test Author`
   - Price: `19.99`
3. Click "Save"
4. Verify product appears in list

### Test 3: Update Product

1. Find the test product (TEST001)
2. Click "Edit"
3. Change price to `24.99`
4. Click "Save"
5. Verify price updated in list

### Test 4: Deactivate Product

1. Find an active product
2. Click "Deactivate"
3. Verify product status changes to "Inactive"
4. Verify product no longer appears in customer-facing catalog

### Test 5: View Audit Log

1. Navigate to `/admin/audit`
2. Verify audit entries for your actions
3. Try filtering by product ASIN
4. Try filtering by date range

### Test 6: Role-Based Access

1. Logout
2. Login as `editor` (password: `Admin123!`)
3. Verify you can create/update products
4. Verify you CANNOT delete products
5. Logout
6. Login as `viewer`
7. Verify you can only view products (no edit buttons)

---

## Step 8: API Testing with cURL

### Test Authentication

```bash
# Login and get JWT token
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'

# Save the returned token
export TOKEN="<your-jwt-token>"
```

### Test Product Listing

```bash
# Get all products
curl -X GET "http://localhost:8084/api/admin/products?page=0&size=10" \
  -H "Authorization: Bearer $TOKEN"

# Search products
curl -X GET "http://localhost:8084/api/admin/products?search=Great" \
  -H "Authorization: Bearer $TOKEN"
```

### Test Product Creation

```bash
# Create new product
curl -X POST http://localhost:8084/api/admin/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "asin": "TEST002",
    "title": "API Test Product",
    "category": "Fiction",
    "author": "Test Author",
    "price": 15.99,
    "quantity": 100
  }'
```

### Test Product Update

```bash
# Update product
curl -X PUT http://localhost:8084/api/admin/products/TEST002 \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "API Test Product - Updated",
    "category": "Fiction",
    "author": "Test Author",
    "price": 17.99,
    "quantity": 100,
    "version": 0
  }'
```

### Test Deactivation

```bash
# Deactivate product
curl -X PATCH http://localhost:8084/api/admin/products/TEST002/deactivate \
  -H "Authorization: Bearer $TOKEN"
```

### Test Audit Logs

```bash
# Get product audit history
curl -X GET "http://localhost:8084/api/admin/audit/products/TEST002" \
  -H "Authorization: Bearer $TOKEN"
```

---

## Running Tests

### Backend Tests (admin-microservice)

```bash
cd admin-microservice

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AdminProductControllerTest

# Run with coverage report
mvn test jacoco:report
# Report at: target/site/jacoco/index.html
```

### Frontend Tests (react-ui)

```bash
cd react-ui/frontend

# Run all tests
npm test

# Run tests in watch mode
npm test -- --watch

# Run tests with coverage
npm test -- --coverage
# Report at: coverage/lcov-report/index.html
```

### Integration Tests

```bash
cd admin-microservice

# Run integration tests with TestContainers
mvn verify -Pintegration-tests

# This starts a real YugabyteDB container for testing
```

---

## Troubleshooting

### Issue: Port Already in Use

```bash
# Find process using port 8084
lsof -i :8084

# Kill process
kill -9 <PID>
```

### Issue: YugabyteDB Connection Failed

```bash
# Check YugabyteDB status
yugabyted status

# Restart YugabyteDB
yugabyted stop
yugabyted start
```

### Issue: Eureka Registration Failed

```bash
# Verify Eureka is running
curl http://localhost:8761

# Check admin-microservice logs for registration errors
# Look for: "Registering application ADMIN-MICROSERVICE"
```

### Issue: JWT Token Expired

```bash
# Tokens expire after 30 minutes (session timeout)
# Simply login again to get a new token
```

### Issue: Schema Not Found

```bash
# Verify YSQL schema
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -c "\dt"

# Verify YCQL schema
cqlsh -e "DESCRIBE TABLE products;"

# Re-run schema scripts if missing
```

### Issue: Cannot Login (Invalid Credentials)

```bash
# Verify admin users exist
ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -c "SELECT * FROM admin_users;"

# Re-create admin user if missing (see Step 3.4)
```

---

## Development Workflow

### Hot Reload for Backend

```bash
# Use Spring Boot DevTools (included in pom.xml)
cd admin-microservice
mvn spring-boot:run

# Code changes auto-reload (no restart needed)
```

### Hot Reload for Frontend

```bash
# React development server has hot reload by default
cd react-ui/frontend
npm start

# Save any .js file and browser auto-refreshes
```

### Database Migrations

When modifying schemas:

1. Create new migration script (e.g., `schema-admin-v2.sql`)
2. Apply to local database
3. Test thoroughly
4. Commit migration script to version control

---

## API Documentation

### Swagger UI (OpenAPI)

Once admin-microservice is running, access interactive API docs:

**URL**: http://localhost:8084/swagger-ui.html

Features:
- Browse all endpoints
- Test API calls directly from browser
- View request/response schemas
- See authentication requirements

### OpenAPI Spec File

Static OpenAPI specification is available at:
- **File**: `specs/002-admin-portal/contracts/admin-api.yaml`
- **Viewer**: Import into Postman or Swagger Editor

---

## Next Steps

1. **Implement User Stories**: Start with P1 user stories from `spec.md`
2. **Write Tests**: Follow TDD approach (tests first, then implementation)
3. **Review Data Model**: Reference `data-model.md` for entity details
4. **Consult Research**: See `research.md` for architecture decisions
5. **Generate Tasks**: Run `/speckit.tasks` to break down implementation

---

## Quick Reference

### Service Ports

| Service | Port | URL |
|---------|------|-----|
| Eureka Server | 8761 | http://localhost:8761 |
| Products Service | 8081 | http://localhost:8081 |
| Admin Service | 8084 | http://localhost:8084 |
| API Gateway | 8080 | http://localhost:8080 |
| React UI | 3000 | http://localhost:3000 |
| YugabyteDB YSQL | 5433 | postgresql://localhost:5433 |
| YugabyteDB YCQL | 9042 | localhost:9042 |
| YugabyteDB UI | 15433 | http://localhost:15433 |

### Default Admin Users

| Username | Password | Role | Permissions |
|----------|----------|------|-------------|
| admin | Admin123! | ADMIN | Full access (create, update, delete) |
| editor | Admin123! | EDITOR | Create and update only |
| viewer | Admin123! | VIEWER | Read-only access |

### Key Files

| File | Description |
|------|-------------|
| `spec.md` | Feature specification |
| `plan.md` | Implementation plan (this document's parent) |
| `research.md` | Technical decisions |
| `data-model.md` | Entity schemas |
| `contracts/admin-api.yaml` | OpenAPI specification |
| `admin-microservice/src/main/resources/schema-admin.sql` | YSQL schema |
| `resources/schema-products-v2.cql` | YCQL schema extension |

---

## Support

- **Issues**: https://github.com/YugabyteDB-Samples/yugastore-java/issues
- **Docs**: See `docs/software-engineer-guide.md`
- **Team Chat**: [Your team chat link]

---

**Total Setup Time**: ~15-20 minutes (first time)
**Time to First API Call**: ~5 minutes (after setup complete)
