# Admin Portal Quick Start Guide

This guide will help you get the YugaStore Admin Portal up and running in under 10 minutes.

## Prerequisites

Before starting, ensure you have:

- ✅ YugabyteDB running (YSQL on 5433, YCQL on 9042)
- ✅ Java 17+ installed
- ✅ Maven 3.6+ installed
- ✅ Node.js 14+ (for React frontend)
- ✅ Eureka Server running on port 8761

## Quick Setup (5 Steps)

### Step 1: Initialize Database Schemas

```bash
# Navigate to project root
cd /path/to/yugastore-java

# Initialize YSQL schema (admin users and audit logs)
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte \
  -f admin-microservice/src/main/resources/schema-admin.sql

# Initialize YCQL schema (product catalog extensions)
cqlsh 127.0.0.1 9042 -f resources/schema-products-v2.cql
```

### Step 2: Create Admin Users

```bash
# Create default test users (admin, editor, viewer)
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte \
  -f resources/seed-admin-users.sql
```

**Default Users Created:**
- Username: `admin` / Password: `admin123` / Role: ADMIN
- Username: `editor` / Password: `editor123` / Role: EDITOR
- Username: `viewer` / Password: `viewer123` / Role: VIEWER

### Step 3: Build and Start Admin Microservice

```bash
# Build the admin microservice
cd admin-microservice
mvn clean package

# Start the service
java -jar target/admin-microservice-0.0.1-SNAPSHOT.jar
```

Or use Maven directly:
```bash
mvn spring-boot:run
```

**Service starts on port 8084**

### Step 4: Verify Service Health

```bash
# Check health endpoint
curl http://localhost:8084/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

### Step 5: Test Authentication

```bash
# Login with admin user
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Expected response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 1800,
  "user": {
    "userId": "...",
    "username": "admin",
    "role": "ADMIN",
    "email": "admin@yugastore.com",
    "createdAt": "...",
    "lastLogin": "..."
  }
}
```

## Using the Admin Portal

### Login via React UI

1. **Start the React frontend** (if not already running):
```bash
cd react-ui/frontend
npm start
```

2. **Navigate to admin portal**:
```
http://localhost:3000/admin/login
```

3. **Login with test credentials**:
   - Username: `admin`
   - Password: `admin123`

4. **You'll be redirected to the products page**

### Using the API

Once you have a JWT token, use it in the Authorization header:

```bash
# Get current user info
curl http://localhost:8084/api/admin/auth/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Logout
curl -X POST http://localhost:8084/api/admin/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Testing Different Roles

### Test as ADMIN (Full Access)
```bash
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Can: Create, Read, Update, Delete products

### Test as EDITOR (Create & Update)
```bash
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"editor","password":"editor123"}'
```

Can: Create, Read, Update products
Cannot: Delete products

### Test as VIEWER (Read-Only)
```bash
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"viewer","password":"viewer123"}'
```

Can: Read products and audit logs
Cannot: Create, Update, or Delete products

## Troubleshooting

### "Connection refused" to YugabyteDB

**Solution:**
```bash
# Check YugabyteDB status
yugabyted status

# If not running, start it
yugabyted start

# Verify YSQL
psql -h 127.0.0.1 -p 5433 -U yugabyte

# Verify YCQL
cqlsh 127.0.0.1 9042
```

### "Failed to authenticate"

**Possible causes:**
1. Wrong username/password
2. User doesn't exist in database
3. User is inactive or locked

**Solution:**
```bash
# Check if users exist
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte \
  -c "SELECT username, role, is_active FROM admin_users;"

# Reset a user's password (requires generating new BCrypt hash)
# See resources/generate-admin-user.sh
```

### "Failed to register with Eureka"

**Solution:**
```bash
# Check if Eureka is running
curl http://localhost:8761

# If not, start Eureka server
cd eureka-server-local
mvn spring-boot:run
```

### Build fails with "Cannot resolve dependencies"

**Solution:**
```bash
# Clean Maven cache and rebuild
mvn clean install -U
```

### JWT token expired

Tokens expire after 30 minutes. Simply login again to get a new token.

```bash
# The frontend handles this automatically
# For API testing, get a fresh token:
curl -X POST http://localhost:8084/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Configuration

### Change JWT Secret (Production)

Edit `admin-microservice/src/main/resources/application.yml`:

```yaml
jwt:
  secret: YOUR_SECURE_SECRET_KEY_HERE_MINIMUM_256_BITS
  expiration: 1800000  # 30 minutes
```

### Change Session Timeout

```yaml
admin:
  session-timeout: 3600  # 1 hour in seconds
```

### Change Max Login Attempts

```yaml
admin:
  max-login-attempts: 5
  lockout-duration: 300  # 5 minutes in seconds
```

## Next Steps

Now that the admin portal is running, you can:

1. **Continue Implementation**: Proceed with Phase 3-5 to add:
   - Product search and filtering
   - Product CRUD operations
   - Bulk operations
   - Audit history viewer

2. **Customize Configuration**: Update application.yml for your environment

3. **Add More Users**: Use `resources/generate-admin-user.sh` to create additional admin users

4. **Secure for Production**: Follow the production deployment checklist in admin-microservice/README.md

5. **Monitor the Service**: Use Actuator endpoints for health checks and metrics

## API Documentation

For complete API documentation, see:
- [Admin Microservice README](../admin-microservice/README.md)
- [API Specification](../specs/002-admin-portal/contracts/admin-api.yaml)

## Getting Help

If you encounter issues:
1. Check the [Troubleshooting](#troubleshooting) section above
2. Review logs in `admin-microservice/logs/`
3. Consult the main [README](../README.md)
4. Create an issue in the GitHub repository

## Development vs Production

| Feature              | Development (Current) | Production (Required)          |
|----------------------|----------------------|--------------------------------|
| JWT Secret           | Default in config    | ✅ Must change                 |
| Admin Passwords      | admin123, etc.       | ✅ Must change                 |
| HTTPS                | Not required         | ✅ Must enable                 |
| CORS Origins         | localhost:3000       | ✅ Update for production URL   |
| Database Credentials | yugabyte/yugabyte    | ✅ Use secure credentials      |
| Logging Level        | DEBUG                | ✅ Change to INFO or WARN      |
| Rate Limiting        | Not enabled          | ✅ Should enable               |

**⚠️ WARNING**: Never deploy to production with default passwords and JWT secret!

## Architecture Overview

```
┌─────────────────┐
│  React Frontend │
│  (Port 3000)    │
└────────┬────────┘
         │ HTTP + JWT
         ▼
┌─────────────────┐      ┌──────────────┐
│ API Gateway     │◄────►│ Eureka       │
│ (Port 8081)     │      │ (Port 8761)  │
└────────┬────────┘      └──────────────┘
         │ Routes /api/admin/*
         ▼
┌─────────────────┐
│ Admin Service   │
│ (Port 8084)     │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│     YugabyteDB              │
│ ┌─────────┐  ┌────────────┐│
│ │  YSQL   │  │    YCQL    ││
│ │(5433)   │  │   (9042)   ││
│ │         │  │            ││
│ │ - admin_│  │ - products ││
│ │   users │  │            ││
│ │ - audit_│  │            ││
│ │   log   │  │            ││
│ └─────────┘  └────────────┘│
└─────────────────────────────┘
```

## Summary

You now have:
- ✅ Admin microservice running on port 8084
- ✅ Database schemas initialized
- ✅ Test users created (admin, editor, viewer)
- ✅ JWT authentication working
- ✅ React frontend with login page
- ✅ Role-based access control configured

**Ready to proceed with product management features!**
