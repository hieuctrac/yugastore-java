# YugaStore Constitution
<!-- E-commerce microservices platform with Spring Boot & YugabyteDB -->

## Core Principles

### I. Microservice Independence
Each service must be self-contained with clear domain boundaries. Services communicate only through well-defined APIs (REST/OpenFeign). No shared databases between services. Each service owns its data and business logic completely.

**Rationale**: Enables independent deployment, scaling, and team ownership. Prevents cascading failures and tight coupling.

### II. Polyglot Persistence Strategy
Use YSQL (PostgreSQL-compatible) for transactional data requiring ACID properties. Use YCQL (Cassandra-compatible) for high-scale, read-heavy workloads. Match database choice to data access patterns, not convenience.

**Current mapping**:
- Cart, Login, Admin: YSQL (transactional integrity)
- Products, Checkout, Orders: YCQL (scale and performance)

### III. Security-First Design (NON-NEGOTIABLE)
JWT-based authentication with proper RBAC. All endpoints secured by default - explicit @PreAuthorize annotations required. Input validation at service boundaries. Audit logging for all admin operations. Never trust client-side validation alone.

### IV. Observability & Monitoring
Spring Actuator endpoints enabled on all services. Structured logging with correlation IDs for request tracing. Health checks must reflect actual service health, not just "UP". Monitor both technical metrics and business metrics.

### V. API-First Development
OpenAPI specifications before implementation. Contract testing between services. Versioning strategy for breaking changes (prefer additive changes). Error responses follow consistent JSON structure across all services.

## Technology Standards

### Required Stack
- **Language**: Java 17 (LTS)
- **Framework**: Spring Boot 2.6.3, Spring Cloud 2021.0.0
- **Database**: YugabyteDB (YSQL + YCQL as appropriate)
- **Service Discovery**: Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Authentication**: JWT with Spring Security
- **Build Tool**: Maven with Spring Boot parent

### Approved Libraries
- Jackson for JSON processing
- Spring Data JPA (for YSQL services)
- Spring Data Cassandra (for YCQL services)
- OpenFeign for inter-service communication
- Spring Boot Actuator for monitoring
- Hibernate Validator for input validation

### Technology Constraints
- No direct database connections between services
- No synchronous calls in critical user flows (prefer async where possible)
- No business logic in controllers (use service layer)
- No SQL queries in controllers (use repository/service layer)

## Development Workflow

### Feature Development Process
1. **Specification**: Use `/specify` to capture requirements and user scenarios
2. **Planning**: Use `/plan` to design implementation approach
3. **Task Breakdown**: Use `/tasks` to create actionable work items
4. **Implementation**: Follow TDD where feasible, test API contracts
5. **Documentation**: Update service README and API documentation

### Code Review Requirements
- All changes require spec-driven development (spec.md exists)
- JWT security implications reviewed for auth-related changes
- Database schema changes reviewed for YSQL/YCQL appropriateness
- Inter-service API changes require contract validation
- Performance implications considered for high-volume endpoints (products, cart)

### Quality Gates
- Unit tests for business logic
- Integration tests for API endpoints
- Contract tests for inter-service communication
- Security validation for authentication/authorization changes
- Performance testing for database query changes

## Governance

This constitution supersedes individual preferences and must be followed for all changes. Amendments require:
1. Documented justification with technical reasoning
2. Impact assessment on existing services
3. Team consensus and update to this document

**Special Considerations**:
- YugabyteDB-specific optimizations take precedence over generic patterns
- E-commerce domain requirements (inventory, pricing, cart) override general web app patterns
- Security requirements are non-negotiable due to customer data handling

Use this constitution as the definitive guide for architectural decisions. When in doubt, choose the approach that maintains service independence and security.

**Version**: 1.0.0 | **Ratified**: 2026-01-27 | **Last Amended**: 2026-01-27
