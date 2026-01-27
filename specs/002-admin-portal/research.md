# Research: Admin Portal for Product Management

**Feature**: 002-admin-portal
**Date**: 2026-01-27
**Status**: Complete

## Overview

This document captures technical research and decisions for implementing the Admin Portal feature. All NEEDS CLARIFICATION items from Technical Context have been resolved.

---

## 1. Microservice Architecture Decision

### Decision
Create a new **admin-microservice** as a separate Spring Boot microservice, following the existing YugaStore pattern.

### Rationale
- **Consistency**: Matches the architecture of products-microservice, cart-microservice, checkout-microservice
- **Separation of Concerns**: Admin operations are a distinct domain from customer-facing operations
- **Independent Scaling**: Admin and customer workloads can scale independently
- **Security Isolation**: Admin functions are isolated at the service level
- **Team Familiarity**: Development team is experienced with this pattern

### Alternatives Considered

**Alternative 1: Extend products-microservice**
- Pros: Simpler deployment, direct database access, fewer moving parts
- Cons: Mixes customer and admin concerns, harder to apply different security policies, products-microservice would become bloated
- **Rejected**: Violates separation of concerns, makes products-microservice responsible for two different domains

**Alternative 2: Serverless functions (AWS Lambda, etc.)**
- Pros: Auto-scaling, pay-per-use, no server management
- Cons: Breaks from existing architecture, introduces new deployment paradigm, team learning curve, cold start latency
- **Rejected**: Inconsistent with existing stack, would require significant infrastructure changes

---

## 2. Authentication & Authorization Approach

### Decision
Use **Spring Security with JWT tokens** for stateless authentication and session management.

### Rationale
- **Stateless**: JWT tokens enable horizontal scaling without shared session storage
- **Standard**: Industry-standard approach for REST APIs
- **Spring Security Integration**: First-class support in Spring Boot
- **Expire & Refresh**: Built-in token expiration (30-minute timeout requirement)
- **Role-Based Access**: Spring Security's role-based authorization fits Admin/Editor/Viewer requirement perfectly

### Implementation Details
```
Login Flow:
1. POST /api/admin/auth/login (username + password)
2. Server validates credentials against admin_users table (BCrypt password hash)
3. Server generates JWT with user_id, username, role, expiration
4. Client stores JWT (sessionStorage or httpOnly cookie)
5. Client includes JWT in Authorization header for all subsequent requests
6. Server validates JWT on each request and extracts role for authorization

Token Structure (JWT Claims):
- sub: user_id
- username: username
- role: ADMIN | EDITOR | VIEWER
- iat: issued at timestamp
- exp: expiration timestamp (30 minutes from iat)
```

### Alternatives Considered

**Alternative 1: Session-based authentication (server-side sessions)**
- Pros: Simpler, can invalidate sessions server-side immediately
- Cons: Requires shared session storage (Redis/database) for horizontal scaling, stateful architecture
- **Rejected**: Doesn't scale horizontally as easily, adds session storage dependency

**Alternative 2: OAuth2 with external identity provider**
- Pros: Enterprise-grade, supports SSO, offloads user management
- Cons: Requires external IdP setup (Okta, Auth0, etc.), overkill for demo app, added complexity
- **Rejected**: Over-engineered for YugaStore's scope, PRD indicates basic username/password is sufficient

**Alternative 3: API keys**
- Pros: Very simple
- Cons: No user context, no expiration, difficult to manage per-user permissions
- **Rejected**: Doesn't support role-based access or session timeout requirement

---

## 3. Database Strategy

### Decision
Use **YSQL (PostgreSQL-compatible) for admin-specific tables**, extend YCQL products schema for is_active field.

### Rationale
- **YSQL for Admin Data**: Relational data (admin users, audit logs with foreign keys) fits YSQL well
- **YCQL for Products**: Products already in YCQL (Cassandra-compatible), minimize disruption
- **Consistency**: Products-microservice uses YCQL, admin-microservice can query via Feign client or shared schema extension
- **YugabyteDB Multi-API**: Single database, both APIs supported

### Schema Design

**New YSQL Tables (admin-microservice):**
```sql
-- Admin users and roles
CREATE TABLE admin_users (
    user_id UUID PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EDITOR', 'VIEWER')),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP
);

-- Product audit log
CREATE TABLE product_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    product_asin VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL REFERENCES admin_users(user_id),
    action_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    ip_address INET,
    timestamp TIMESTAMP DEFAULT NOW()
);

CREATE INDEX idx_audit_product ON product_audit_log(product_asin, timestamp DESC);
CREATE INDEX idx_audit_user ON product_audit_log(user_id, timestamp DESC);
CREATE INDEX idx_audit_timestamp ON product_audit_log(timestamp DESC);
```

**Modified YCQL Schema (products-microservice):**
```cql
-- Add to existing products table
ALTER TABLE products ADD is_active BOOLEAN;
ALTER TABLE products ADD deactivated_at TIMESTAMP;
ALTER TABLE products ADD deactivated_by VARCHAR;

-- Create index for filtering active/inactive
CREATE INDEX ON products (is_active);
```

### Alternatives Considered

**Alternative 1: All data in YSQL**
- Pros: Single query language, relational features for everything
- Cons: Requires migrating existing products from YCQL to YSQL (major disruption)
- **Rejected**: Too much migration risk, products-microservice already optimized for YCQL

**Alternative 2: All data in YCQL**
- Pros: Consistency, no multi-API complexity
- Cons: Audit logs with foreign keys and complex queries less natural in Cassandra model
- **Rejected**: Audit logs are relational by nature (joins, aggregations, filtering)

---

## 4. Integration with products-microservice

### Decision
Use **Feign Client** for admin-microservice to call products-microservice, plus direct YCQL access for performance-critical operations.

### Rationale
- **Feign is Standard**: Already used in checkout-microservice to call cart-microservice
- **Service Boundary Respect**: Admin calls products via API, maintains service independence
- **Direct YCQL for Reads**: Admin can read products directly from YCQL for list/search performance
- **Updates via Products API**: Write operations go through products-microservice to maintain business logic consistency

### Implementation Pattern
```java
// Feign client in admin-microservice
@FeignClient(name = "products-microservice")
public interface ProductCatalogRestClient {
    @GetMapping("/products/{asin}")
    ProductDto getProduct(@PathVariable String asin);

    @PutMapping("/products/{asin}")
    ProductDto updateProduct(@PathVariable String asin, @RequestBody ProductUpdateDto dto);
}

// OR: Direct YCQL access for reads
@Repository
public interface ProductReadRepository extends CassandraRepository<ProductMetadata, String> {
    @Query("SELECT * FROM products WHERE is_active = true")
    List<ProductMetadata> findAllActive();
}
```

### Alternatives Considered

**Alternative 1: Shared database access only**
- Pros: No network calls, faster
- Cons: Breaks service boundaries, products-microservice business logic bypassed, harder to maintain
- **Rejected**: Violates microservice principles

**Alternative 2: Duplicate product data in admin-microservice database**
- Pros: Admin fully independent, no cross-service calls
- Cons: Data synchronization complexity, eventual consistency issues, storage duplication
- **Rejected**: Unnecessary complexity, products are source of truth

---

## 5. Frontend Framework & Integration

### Decision
Integrate admin UI into **existing react-ui** as new routes under `/admin/*`.

### Rationale
- **Code Reuse**: Share React components, utilities, build setup
- **Single Deployment**: One React app, simpler CI/CD
- **Existing Pattern**: YugaStore already uses react-ui for all frontend
- **React Router**: Easy to separate admin routes from customer routes
- **Simpler**: Fewer projects to maintain

### Routing Strategy
```javascript
// App.js
<Router>
  <Switch>
    {/* Customer routes */}
    <Route exact path="/" component={Home} />
    <Route path="/products" component={Products} />
    <Route path="/cart" component={Cart} />

    {/* Admin routes */}
    <Route path="/admin/login" component={AdminLogin} />
    <PrivateRoute path="/admin" roles={['ADMIN', 'EDITOR', 'VIEWER']}>
      <AdminLayout>
        <Switch>
          <Route path="/admin/products" component={AdminProductList} />
          <Route path="/admin/products/new" component={ProductCreateForm} />
          <Route path="/admin/products/:asin" component={ProductEditForm} />
          <Route path="/admin/audit" component={AuditLogViewer} />
        </Switch>
      </AdminLayout>
    </PrivateRoute>
  </Switch>
</Router>
```

### Alternatives Considered

**Alternative 1: Separate admin-ui React app**
- Pros: Complete isolation, independent deployment
- Cons: Duplicate dependencies, can't share components, more deployment complexity
- **Rejected**: Unnecessary isolation, adds maintenance overhead

**Alternative 2: Server-side rendered admin (Thymeleaf, JSP)**
- Pros: Simpler for CRUD forms, no frontend build step
- Cons: Inconsistent with existing React frontend, worse UX, less interactive
- **Rejected**: Users expect modern SPA experience, inconsistent with customer UI

---

## 6. API Design Pattern

### Decision
Use **RESTful API with JSON** for all admin endpoints.

### Rationale
- **Consistency**: Matches existing YugaStore microservice APIs
- **Standard**: REST is well-understood by team
- **Tooling**: Spring Boot has excellent REST support
- **Swagger**: Can generate OpenAPI documentation automatically

### Endpoint Structure
```
Authentication:
POST   /api/admin/auth/login
POST   /api/admin/auth/logout
GET    /api/admin/auth/me

Product Management:
GET    /api/admin/products              # List with filters, pagination
GET    /api/admin/products/{asin}       # Get single product
POST   /api/admin/products              # Create product
PUT    /api/admin/products/{asin}       # Update product
PATCH  /api/admin/products/{asin}/deactivate
PATCH  /api/admin/products/{asin}/activate
DELETE /api/admin/products/{asin}       # Hard delete

Bulk Operations:
POST   /api/admin/products/bulk-update  # Bulk update
POST   /api/admin/products/bulk-deactivate

Audit:
GET    /api/admin/audit/products/{asin} # Product change history
GET    /api/admin/audit/logs            # All audit logs with filters
```

### Alternatives Considered

**Alternative 1: GraphQL**
- Pros: Flexible queries, single endpoint, better for complex data fetching
- Cons: Team unfamiliar, added complexity, overkill for CRUD operations
- **Rejected**: REST is sufficient for admin CRUD operations

**Alternative 2: gRPC**
- Pros: Performance, type safety, efficient binary protocol
- Cons: Less HTTP-native, harder browser integration, team unfamiliar
- **Rejected**: REST + JSON is more accessible, browser-friendly

---

## 7. Testing Strategy

### Decision
**JUnit 5 + Mockito + Spring Boot Test + TestContainers** for backend, **React Testing Library** for frontend.

### Rationale
- **Consistency**: Matches existing YugaStore testing approach
- **TestContainers**: Run real YugabyteDB in tests for integration testing
- **Spring Boot Test**: Excellent Spring context testing support
- **React Testing Library**: Modern, user-centric component testing

### Test Pyramid
```
Backend:
- Unit Tests: Service layer logic (Mockito mocks)
- Integration Tests: Controller → Service → Repository (TestContainers)
- Contract Tests: Feign client interactions

Frontend:
- Component Tests: React Testing Library
- Integration Tests: Full user flows with mocked API
```

### Coverage Goals
- Backend: 80% code coverage (per PRD NFR-5.1)
- Frontend: 70% component coverage (focus on critical paths)

---

## 8. Bulk Operations Implementation

### Decision
Use **asynchronous processing with progress tracking** for bulk operations.

### Rationale
- **User Experience**: Large bulk operations (100+ products) shouldn't block the UI
- **Timeout Avoidance**: HTTP requests can timeout on long operations
- **Progress Feedback**: Users see real-time progress and can continue working

### Implementation Approach
```
1. POST /api/admin/products/bulk-update
   - Validates request
   - Creates bulk operation job
   - Returns job_id immediately

2. Backend processes job asynchronously (Spring @Async)
   - Updates products one by one
   - Tracks success/failure counts

3. GET /api/admin/jobs/{job_id}
   - Frontend polls for status
   - Returns: { status: 'IN_PROGRESS' | 'COMPLETED', processed: 50, total: 100, failures: [] }

4. Frontend shows progress bar
   - Updates every 2 seconds
   - Shows success message when completed
```

### Alternatives Considered

**Alternative 1: Synchronous bulk updates**
- Pros: Simpler implementation, immediate result
- Cons: Can timeout, blocks user, poor UX for large operations
- **Rejected**: Doesn't meet UX requirements for "progress indicator"

---

## 9. Concurrency & Optimistic Locking

### Decision
Use **versioning with optimistic locking** for product updates to detect concurrent modifications.

### Rationale
- **Spec Requirement**: FR-021 requires warning users of concurrent edits
- **Performance**: Optimistic locking doesn't block reads, better for admin workload
- **YugabyteDB Support**: Supports compare-and-set operations

### Implementation
```sql
-- Add version column to products
ALTER TABLE products ADD version INT;

-- Update logic (pseudo-code)
UPDATE products
SET title = ?, price = ?, version = version + 1
WHERE asin = ? AND version = ?
```

If update affects 0 rows → version mismatch → return 409 Conflict.

Frontend shows: "This product was modified by [user] at [time]. Reload to see latest changes."

### Alternatives Considered

**Alternative 1: Pessimistic locking (SELECT FOR UPDATE)**
- Pros: Guaranteed no conflicts
- Cons: Blocks other users, can cause deadlocks, worse for admin UX
- **Rejected**: Admin users unlikely to edit same product simultaneously, optimistic locking is sufficient

---

## 10. Security Considerations

### Decision
Implement **defense-in-depth** with multiple security layers:

1. **HTTPS Only**: Enforce TLS for all admin traffic
2. **JWT with Short Expiration**: 30-minute token lifetime
3. **Role-Based Authorization**: Method-level @PreAuthorize checks
4. **Input Validation**: JSR-303 Bean Validation on all DTOs
5. **SQL Injection Protection**: Parameterized queries (JPA/Spring Data)
6. **CSRF Protection**: Token-based for state-changing operations
7. **Rate Limiting**: 100 requests/minute per user (Spring Cloud Gateway)
8. **Audit Logging**: All write operations logged with user context

### Rationale
- **PRD Requirements**: NFR-1.1 through NFR-1.5 mandate security controls
- **Best Practices**: Defense-in-depth prevents single point of failure
- **Compliance**: Audit logging supports compliance and forensics

---

## Summary of Key Decisions

| Area | Decision | Rationale |
|------|----------|-----------|
| Architecture | New admin-microservice | Separation of concerns, consistent with existing pattern |
| Authentication | Spring Security + JWT | Stateless, scalable, standard |
| Database | YSQL for admin tables, YCQL for products | Best fit for each data model |
| Integration | Feign client + direct YCQL reads | Balance between service boundaries and performance |
| Frontend | Integrate into react-ui | Code reuse, simpler deployment |
| API Design | RESTful JSON | Consistency, team familiarity |
| Testing | JUnit + Mockito + TestContainers | Matches existing stack |
| Bulk Operations | Async with progress tracking | Better UX, avoids timeouts |
| Concurrency | Optimistic locking | Performance + conflict detection |
| Security | Defense-in-depth, JWT, RBAC | Comprehensive protection |

All technical unknowns from the Technical Context section have been resolved. Ready to proceed to Phase 1 (Data Model & Contracts).
