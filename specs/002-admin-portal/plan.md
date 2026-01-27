# Implementation Plan: Admin Portal for Product Management

**Branch**: `002-admin-portal` | **Date**: 2026-01-27 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/002-admin-portal/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Enable authorized personnel to manage the YugaStore product catalog through a web-based administrative interface. The admin portal will provide comprehensive product CRUD operations (create, read, update, delete with soft/hard delete), role-based access control (Admin, Editor, Viewer roles), audit logging, and bulk operations. This addresses the current pain point of requiring developer intervention for routine product management tasks, reducing product update time from hours to minutes.

## Technical Context

**Language/Version**: Java 17 (matching existing YugaStore microservices)
**Primary Dependencies**:
- Spring Boot 2.6.3 (backend microservice framework)
- Spring Security (authentication and authorization)
- Spring Data JPA (database access layer)
- Spring Cloud Netflix Eureka (service discovery)
- React 16.2.0 (frontend UI framework)
- React Bootstrap 0.32.4 (UI components)
- Axios 0.18.0 (HTTP client)
- Maven 3.6+ (build tool)

**Storage**: YugabyteDB (YSQL for admin data, YCQL for product catalog integration)
- Admin users and roles → YSQL tables
- Product audit logs → YSQL tables
- Product catalog → YCQL (existing products-microservice schema, extend with is_active field)

**Testing**:
- JUnit 5 (unit tests)
- Mockito (mocking framework)
- Spring Boot Test (integration tests)
- TestContainers (database integration tests)
- React Testing Library (frontend tests)

**Target Platform**: Linux server (Docker containers), web browsers (Chrome, Firefox, Safari latest versions)

**Project Type**: Web application (microservice backend + React frontend)

**Performance Goals**:
- Product list load: < 2 seconds
- Search/filter results: < 2 seconds
- Product update/save: < 1 second
- Support 50 concurrent admin users
- Bulk operations: 100 products/minute minimum

**Constraints**:
- API response time: < 500ms (95th percentile)
- Page load time: < 3 seconds (90th percentile)
- Database query optimization required (proper indexing on ASIN, category, price, is_active)
- Session timeout: 30 minutes inactivity
- Audit log retention: 90 days minimum
- WCAG 2.1 Level AA compliance (accessibility)
- HTTPS only (no HTTP)

**Scale/Scope**:
- Catalog size: up to 100,000 products
- Concurrent admin users: 50
- Admin roles: 3 (Admin, Editor, Viewer)
- API endpoints: ~15 endpoints
- Frontend pages: ~8 main views
- Expected development: 1 new microservice + 1 new React admin UI module

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

**Status**: N/A - Project constitution not yet defined

The `.specify/memory/constitution.md` file contains only a template and has not been populated with project-specific architectural principles and constraints. For this feature, we will proceed following the existing YugaStore architectural patterns observed in the codebase:

**Observed Patterns to Follow**:
1. **Microservice Architecture**: Each domain service is a separate Spring Boot microservice
2. **Service Discovery**: Eureka for service registration and discovery
3. **Database Per Service**: Each microservice manages its own database schema
4. **API Gateway Pattern**: Central gateway for routing and aggregation
5. **Spring Boot Conventions**: Standard Spring Boot project structure with controllers, services, repositories, and domain models
6. **Testing Strategy**: Unit tests with JUnit, integration tests with Spring Boot Test

**Decision for Admin Portal**: Follow the same microservice pattern by creating a new `admin-microservice` that mirrors the structure of existing services (products-microservice, cart-microservice, etc.).

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
yugastore-java/
├── admin-microservice/                 # NEW: Admin portal backend microservice
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       │   └── com/yugabyte/app/yugastore/admin/
│       │       ├── YugastoreAdmin.java              # Main Spring Boot application
│       │       ├── controller/
│       │       │   ├── AdminAuthController.java     # Authentication endpoints
│       │       │   ├── AdminProductController.java  # Product CRUD endpoints
│       │       │   ├── AdminBulkController.java     # Bulk operations
│       │       │   └── AdminAuditController.java    # Audit log endpoints
│       │       ├── service/
│       │       │   ├── AdminUserService.java
│       │       │   ├── AdminProductService.java
│       │       │   ├── AdminAuditService.java
│       │       │   └── impl/                        # Service implementations
│       │       ├── repository/
│       │       │   ├── AdminUserRepository.java
│       │       │   ├── ProductAuditLogRepository.java
│       │       │   └── ProductCatalogRestClient.java  # Feign client to products-microservice
│       │       ├── domain/
│       │       │   ├── AdminUser.java
│       │       │   ├── ProductAuditLog.java
│       │       │   └── AdminRole.java (enum)
│       │       ├── config/
│       │       │   ├── SecurityConfig.java           # Spring Security configuration
│       │       │   └── YugabyteConfig.java          # Database configuration
│       │       └── dto/                             # Data Transfer Objects
│       │           ├── AdminUserDto.java
│       │           ├── ProductUpdateDto.java
│       │           └── AuditLogDto.java
│       ├── test/java/
│       │   └── com/yugabyte/app/yugastore/admin/
│       │       ├── controller/                      # Controller tests
│       │       ├── service/                         # Service tests
│       │       └── integration/                     # Integration tests
│       └── resources/
│           ├── application.yml                      # Spring Boot configuration
│           ├── schema-admin.sql                     # YSQL schema for admin tables
│           └── bootstrap.yml                        # Eureka configuration
│
├── react-ui/                                        # MODIFIED: Add admin portal UI
│   ├── pom.xml
│   ├── src/main/java/                              # Existing BFF layer
│   └── frontend/
│       ├── package.json
│       ├── public/
│       └── src/
│           ├── index.js
│           ├── App.js                               # MODIFIED: Add admin routes
│           ├── components/
│           │   ├── App/                             # Existing customer components
│           │   ├── Cart/
│           │   ├── Home/
│           │   ├── Products/
│           │   ├── ShowProduct/
│           │   ├── Admin/                           # NEW: Admin portal components
│           │   │   ├── Auth/
│           │   │   │   ├── AdminLogin.js
│           │   │   │   └── PasswordReset.js
│           │   │   ├── Products/
│           │   │   │   ├── ProductList.js
│           │   │   │   ├── ProductSearch.js
│           │   │   │   ├── ProductFilters.js
│           │   │   │   └── ProductRow.js
│           │   │   ├── ProductForm/
│           │   │   │   ├── ProductCreateForm.js
│           │   │   │   ├── ProductEditForm.js
│           │   │   │   └── ProductFormValidation.js
│           │   │   ├── BulkOperations/
│           │   │   │   ├── BulkUpdateModal.js
│           │   │   │   └── BulkProgressIndicator.js
│           │   │   ├── AuditLog/
│           │   │   │   ├── AuditLogViewer.js
│           │   │   │   └── AuditLogFilters.js
│           │   │   └── Layout/
│           │   │       ├── AdminNav.js
│           │   │       └── AdminLayout.js
│           │   └── Common/                          # MODIFIED: Shared components
│           │       ├── ConfirmDialog.js             # NEW: Reusable for admin
│           │       └── Pagination.js                # NEW: Reusable for admin
│           ├── services/
│           │   ├── admin/                           # NEW: Admin API services
│           │   │   ├── authService.js
│           │   │   ├── productService.js
│           │   │   └── auditService.js
│           │   └── apiClient.js                     # MODIFIED: Add admin endpoints
│           ├── utils/
│           │   ├── validators.js                    # NEW: Form validation
│           │   └── formatters.js                    # NEW: Data formatting
│           └── __tests__/
│               └── admin/                           # NEW: Admin component tests
│
├── products-microservice/                           # MODIFIED: Extend existing service
│   └── src/main/resources/
│       └── schema-products-v2.cql                   # NEW: Add is_active field
│
├── api-gateway-microservice/                        # MODIFIED: Add admin routes
│   └── src/main/java/
│       └── com/yugabyte/app/yugastore/
│           └── config/
│               └── GatewayRoutes.java               # Add /api/admin/** routes
│
└── resources/
    └── schema-admin.sql                             # NEW: Admin database schema
```

**Structure Decision**: **Integrated Admin UI within Existing react-ui**

This feature integrates the admin portal into the existing react-ui application:

1. **admin-microservice**: New Spring Boot microservice for admin operations
2. **react-ui/frontend**: Add new `/admin` routes and components to existing React app
3. **Routing Strategy**:
   - Customer routes: `/`, `/products`, `/cart`, etc.
   - Admin routes: `/admin/login`, `/admin/products`, `/admin/audit`, etc.
4. **Code Reuse**: Share common components (Pagination, ConfirmDialog), utilities, and API client setup
5. **Database**: New YSQL tables for admin users and audit logs, extend products schema

**Benefits**:
- Simpler deployment (one React app)
- Code reuse for common components
- Single build process
- Follows existing YugaStore pattern (all UI in react-ui)
- Can use React Router code-splitting to keep admin bundle separate if needed

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**Status**: N/A - No constitution defined, therefore no violations to track.

This section would be used to justify architectural complexity that violates project constitution principles. Since no constitution has been established for YugaStore, this tracking is not applicable for the current feature.

When a constitution is defined in the future, this section should document:
- Any deviations from established architectural patterns
- Justification for added complexity
- Why simpler alternatives were insufficient
