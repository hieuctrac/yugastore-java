# Admin Microservice

Admin Portal microservice for YugaStore product management. Provides authenticated admin users with the ability to manage the product catalog through a web interface.

## Overview

The Admin Microservice is part of the YugaStore application suite and provides:

- **Role-Based Access Control (RBAC)**: Three user roles (Admin, Editor, Viewer) with different permission levels
- **Product Management**: Search, create, update, deactivate, and delete products
- **Audit Logging**: Complete audit trail of all product changes
- **JWT Authentication**: Secure token-based authentication with 30-minute session timeout
- **Dual Database Support**: YSQL for admin users and audit logs, YCQL for product catalog access

## Technology Stack

- **Java 17**
- **Spring Boot 2.6.3**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for YSQL (admin users, audit logs)
- **Spring Data Cassandra** for YCQL (products)
- **YugabyteDB** (YSQL + YCQL)
- **Maven** for build management
- **JaCoCo** for code coverage

## Architecture

### Database Schema

#### YSQL (PostgreSQL-compatible)
- `admin_users`: Admin user accounts with roles and authentication
- `product_audit_log`: Audit trail for all product changes

#### YCQL (Cassandra-compatible)
- `products`: Product catalog (extended with admin fields: is_active, version, deactivated_at, deactivated_by)

### User Roles

| Role   | Permissions                                      |
|--------|--------------------------------------------------|
| ADMIN  | Full access - create, read, update, delete       |
| EDITOR | Create and update products, cannot delete        |
| VIEWER | Read-only access to products and audit logs      |

## Setup Instructions

### Prerequisites

1. **YugabyteDB** running locally or remotely
   - YSQL on port 5433
   - YCQL on port 9042
2. **Java 17** or higher
3. **Maven 3.6+**
4. **Eureka Server** running on port 8761 (for service discovery)

### Database Setup

1. **Initialize YSQL schema** (admin users and audit logs):
```bash
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f src/main/resources/schema-admin.sql
```

2. **Initialize YCQL schema** (product catalog extensions):
```bash
cqlsh 127.0.0.1 9042 -f ../resources/schema-products-v2.cql
```

3. **Create seed admin users**:
```bash
psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f ../resources/seed-admin-users.sql
```

### Configuration

Edit `src/main/resources/application.yml` to configure:

- **Database connections** (YSQL and YCQL)
- **JWT secret** (change in production!)
- **Session timeout** (default: 30 minutes)
- **Max login attempts** (default: 5)
- **Lockout duration** (default: 5 minutes)

### Build and Run

1. **Build the application**:
```bash
mvn clean package
```

2. **Run the application**:
```bash
java -jar target/admin-microservice-0.0.1-SNAPSHOT.jar
```

Or use Maven:
```bash
mvn spring-boot:run
```

The service will start on **port 8084**.

### Verify Service is Running

```bash
curl http://localhost:8084/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## API Endpoints

### Authentication

| Method | Endpoint                         | Description              | Access   |
|--------|----------------------------------|--------------------------|----------|
| POST   | `/api/admin/auth/login`          | Login and get JWT token  | Public   |
| POST   | `/api/admin/auth/logout`         | Logout (clear session)   | Authenticated |
| GET    | `/api/admin/auth/me`             | Get current user info    | Authenticated |
| POST   | `/api/admin/auth/password-reset` | Request password reset   | Public   |

### Products (To be implemented in Phase 3-5)

| Method | Endpoint                                  | Description                  | Role Required    |
|--------|-------------------------------------------|------------------------------|------------------|
| GET    | `/api/admin/products`                     | List/search products         | All              |
| GET    | `/api/admin/products/{asin}`              | Get product details          | All              |
| POST   | `/api/admin/products`                     | Create new product           | ADMIN, EDITOR    |
| PUT    | `/api/admin/products/{asin}`              | Update product               | ADMIN, EDITOR    |
| DELETE | `/api/admin/products/{asin}`              | Delete product permanently   | ADMIN            |
| PATCH  | `/api/admin/products/{asin}/deactivate`   | Soft delete (deactivate)     | ADMIN, EDITOR    |
| PATCH  | `/api/admin/products/{asin}/activate`     | Reactivate product           | ADMIN, EDITOR    |

### Audit Logs (To be implemented in Phase 10)

| Method | Endpoint                              | Description                  | Role Required    |
|--------|---------------------------------------|------------------------------|------------------|
| GET    | `/api/admin/audit/products/{asin}`    | Get product change history   | All              |
| GET    | `/api/admin/audit/logs`               | Get filtered audit logs      | ADMIN            |

## Authentication Flow

1. **User submits credentials** to `/api/admin/auth/login`
2. **Server validates** username and password
3. **Server generates JWT token** with user details and role
4. **Client stores token** in localStorage
5. **Client includes token** in Authorization header for all subsequent requests: `Authorization: Bearer <token>`
6. **Server validates token** on each request and checks permissions
7. **Token expires** after 30 minutes (configurable)

## Security Features

- **Password Hashing**: BCrypt with salt
- **JWT Tokens**: Signed with HS256
- **Session Timeout**: 30 minutes (configurable)
- **Account Lockout**: After 5 failed login attempts (configurable)
- **CORS Protection**: Configured for specific origins
- **Method-Level Security**: `@PreAuthorize` annotations for role-based access
- **SQL Injection Protection**: Parameterized queries via JPA
- **XSS Protection**: Input validation and sanitization

## Audit Logging

All product changes are automatically logged with:
- Product ASIN
- User who made the change
- Action type (CREATE, UPDATE, DELETE, DEACTIVATE, ACTIVATE, BULK_UPDATE)
- Field name (for updates)
- Old and new values
- Reason (for deletes)
- IP address
- Timestamp

Audit logs are retained for 90 days and automatically purged.

## Development

### Running Tests

```bash
mvn test
```

### Code Coverage

```bash
mvn clean test jacoco:report
```

View coverage report at `target/site/jacoco/index.html`

### Hot Reload

Use Spring Boot DevTools for automatic restart on code changes:

```bash
mvn spring-boot:run
```

## Troubleshooting

### Connection Refused to YugabyteDB

- Ensure YugabyteDB is running: `yugabyted status`
- Check YSQL is available: `psql -h 127.0.0.1 -p 5433 -U yugabyte`
- Check YCQL is available: `cqlsh 127.0.0.1 9042`

### JWT Token Errors

- Check JWT secret is configured in `application.yml`
- Verify token hasn't expired (30-minute default)
- Clear browser localStorage and re-login

### Failed to Register with Eureka

- Ensure Eureka server is running on port 8761
- Check `bootstrap.yml` has correct Eureka URL
- Wait 30 seconds for initial registration

### Build Failures

- Ensure Java 17 is installed: `java -version`
- Clean Maven cache: `mvn clean install -U`
- Check internet connection for dependency downloads

## Default Test Users

After running the seed script, the following users are available:

| Username    | Password  | Role   | Purpose                    |
|-------------|-----------|--------|----------------------------|
| admin       | admin123  | ADMIN  | Full access testing        |
| editor      | editor123 | EDITOR | Create/update testing      |
| viewer      | viewer123 | VIEWER | Read-only testing          |

**⚠️ WARNING**: Change these passwords in production!

## Production Deployment

Before deploying to production:

1. ✅ Change JWT secret in `application.yml`
2. ✅ Update default admin passwords
3. ✅ Configure production database URLs
4. ✅ Enable HTTPS/TLS
5. ✅ Set up proper CORS origins
6. ✅ Configure logging levels
7. ✅ Set up monitoring and alerts
8. ✅ Enable rate limiting
9. ✅ Review security settings
10. ✅ Set up database backups

## Monitoring

### Actuator Endpoints

- **Health Check**: `http://localhost:8084/actuator/health`
- **Metrics**: `http://localhost:8084/actuator/metrics`
- **Info**: `http://localhost:8084/actuator/info`

## Contributing

See [CONTRIBUTING.md](../CONTRIBUTING.md) for development guidelines.

## License

See [LICENSE](../LICENSE) for details.

## Support

For issues or questions:
- Create an issue in the GitHub repository
- Contact the YugaStore development team

## Implementation Status

### ✅ Phase 1-2: Foundation Complete
- Project setup and structure
- Database configuration (YSQL + YCQL)
- Authentication and security
- User management
- Audit logging infrastructure
- React frontend layout and login

### 🚧 Phase 3-5: Core Features (Planned)
- Role-based access control (RBAC)
- Product search and filtering
- Product CRUD operations
- Concurrent edit detection
- Bulk operations

### 📋 Phase 6-10: Advanced Features (Planned)
- Product deactivation
- Permanent deletion with safeguards
- Audit history viewer
- Advanced reporting

## Related Documentation

- [Main Project README](../README.md)
- [API Gateway Configuration](../api-gateway-microservice/README.md)
- [YugaStore Architecture](../docs/architecture.md)
- [Feature Specification](../specs/002-admin-portal/spec.md)
- [Implementation Plan](../specs/002-admin-portal/plan.md)
- [Task List](../specs/002-admin-portal/tasks.md)
