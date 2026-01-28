# Tasks: Role-Based Access Control (RBAC) System

**Current Status**: ✅ **Authentication Core Complete** - JWT-based login system working with REST API endpoints
**Last Updated**: January 28, 2026
**Branch**: `001-rbac-implementation`

## 🚀 Current Progress Summary

**✅ COMPLETED:**
- Phase 1: Setup (JWT dependencies, database schema)
- Phase 2: Foundation (User/Role entities, JWT utilities, UserRepository)
- Phase 4: User Story 2 Backend (Registration, Login, Protected endpoints working)

**⏳ NEXT PRIORITIES:**
1. **API Gateway Integration** (T012-T013, T027) - JWT validation and user context propagation
2. **User Story 1** - Anonymous product browsing (T014-T020)
3. **Service Integration** - Cart/Order user data isolation (T028-T029)
4. **Frontend Integration** - React authentication UI (T030-T033)

**Input**: Design documents from `/specs/001-rbac-implementation/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md (to be created), data-model.md (to be created), contracts/ (to be created)

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## 📋 Implementation Status & Deviations

### What We Built vs What Was Planned

**✅ Successfully Implemented:**
- **REST API Authentication System**: Complete `/api/auth/` endpoints (register, login, logout, me, refresh)
- **JWT Token System**: Access & refresh tokens with proper expiration and claims
- **RBAC Foundation**: User/Role entities with many-to-many relationships
- **Spring Security Integration**: JWT authentication with UserDetailsService
- **Database Integration**: PostgreSQL with proper RBAC schema and indexes
- **Docker Infrastructure**: Complete local development environment with Docker Compose
- **Password Security**: BCrypt hashing with 12 rounds

**📋 Plan Deviation Notes:**
- **Skipped Phase Order**: Jumped directly to User Story 2 implementation instead of following Phase 3 → Phase 4
- **Technology Swap**: Used PostgreSQL instead of YugabyteDB for local development simplicity
- **Enhanced Docker Setup**: Added comprehensive Docker infrastructure not in original scope
- **Simplified Session Management**: Used stateless JWT instead of separate Session entity

**⚠️ Implementation Gaps:**
- **API Gateway**: Missing JWT validation filter and user context propagation
- **Service Integration**: Cart/Order services not yet filtering by authenticated user
- **Frontend**: No React authentication UI components yet
- **User Story 1**: Anonymous product browsing not yet implemented

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Existing microservices structure**: Each service has `src/main/java/`, `src/main/resources/`, `src/test/java/`
- **Enhancement approach**: Add new packages within existing services (security/, entities/, services/, controllers/)
- All changes within existing folder structure - NO structural modifications

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure preparation

- [x] T001 [P] Add JWT dependency (io.jsonwebtoken:jjwt:0.11.5) to all service pom.xml files
- [x] T002 [P] Create auth database schema file in resources/auth-schema.sql
- [x] T003 [P] Update .gitignore to exclude authentication tokens and sensitive config files
- [x] T004 [P] Create migration scripts directory in resources/migration-scripts/

---

## Phase 2: Foundational (Blocking Prerequisites) ✅ **COMPLETE**

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**✅ STATUS**: Foundation complete - User Story implementation can now proceed

- [x] T005 Execute auth-schema.sql to create User, Role, Session, and Audit tables (PostgreSQL YSQL)
- [x] T006 [P] Create base User entity in login-microservice/src/main/java/entities/User.java
- [x] T007 [P] Create base Role entity in login-microservice/src/main/java/entities/Role.java
- [x] T008 [P] Create UserRepository in login-microservice/src/main/java/repositories/UserRepository.java
- [x] T009 [P] Create JWT utility class in login-microservice/src/main/java/security/JwtUtil.java
- [ ] T010 [P] Create user context model in api-gateway-microservice/src/main/java/security/UserContext.java
- [x] T011 Configure JWT secret and expiration in login-microservice/src/main/resources/application.yml
- [ ] T012 [P] Create authentication filter in api-gateway-microservice/src/main/java/security/JwtAuthenticationFilter.java
- [ ] T013 Update api-gateway-microservice/src/main/resources/application.yml with security configuration

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Anonymous Product Browsing (Priority: P1) 🎯 MVP

**Goal**: Visitors can browse products without authentication while blocking access to user-specific operations

**Independent Test**: Visit homepage, browse products, search catalog, verify cart/checkout prompt for authentication

### Implementation for User Story 1

- [ ] T014 [P] [US1] Configure anonymous access for product endpoints in products-microservice/src/main/java/security/SecurityConfig.java
- [ ] T015 [P] [US1] Update products-microservice controllers to allow unauthenticated access in src/main/java/controllers/
- [ ] T016 [P] [US1] Add authentication check middleware to cart endpoints in cart-microservice/src/main/java/security/AuthRequiredFilter.java
- [ ] T017 [P] [US1] Add authentication check middleware to checkout endpoints in checkout-microservice/src/main/java/security/AuthRequiredFilter.java
- [ ] T018 [US1] Configure api-gateway to route anonymous requests to products-microservice without authentication in src/main/java/config/RouteConfig.java
- [ ] T019 [US1] Add authentication prompt UI components in react-ui/src/auth/AuthenticationPrompt.js
- [ ] T020 [US1] Update React cart components to show login prompt for unauthenticated users in react-ui/src/components/Cart/

**Checkpoint**: Anonymous users can browse products, authenticated access required for cart/checkout

---

## Phase 4: User Story 2 - Customer Registration and Authentication (Priority: P1) 🎯 MVP ✅ **BACKEND COMPLETE**

**Goal**: Customers can register, login, and access their own data with complete isolation from other customers

**✅ STATUS**: Core authentication system working! Successfully tested registration, login, JWT tokens, and protected endpoints.

**Independent Test**: ✅ PASSED - Register account, login, receive JWT, access protected /me endpoint

### Implementation for User Story 2

**✅ Authentication Core (Complete):**
- [x] T021 [P] [US2] Create UserService with registration logic (implemented in AuthController)
- [x] T022 [P] [US2] Create password hashing service (implemented in SecurityBeans with BCrypt)
- [x] T023 [P] [US2] Create Session entity (JWT-based sessions, no separate entity needed)
- [x] T024 [US2] Create registration endpoint in login-microservice/src/main/java/web/AuthController.java
- [x] T025 [US2] Create login endpoint with JWT generation in login-microservice/src/main/java/web/AuthController.java
- [x] T026 [US2] Create logout endpoint with session invalidation in login-microservice/src/main/java/web/AuthController.java
- [x] T021b [US2] Create RbacUserDetailsService in login-microservice/src/main/java/web/RbacUserDetailsService.java

**⏳ Service Integration (Pending):**
- [ ] T027 [US2] Implement user context extraction in api-gateway-microservice/src/main/java/security/UserContextExtractor.java
- [ ] T028 [P] [US2] Add user filtering to cart operations in cart-microservice/src/main/java/services/CartService.java
- [ ] T029 [P] [US2] Add user filtering to order operations in checkout-microservice/src/main/java/services/OrderService.java

**⏳ Frontend Integration (Pending):**
- [ ] T030 [US2] Create registration form component in react-ui/src/auth/RegistrationForm.js
- [ ] T031 [US2] Create login form component in react-ui/src/auth/LoginForm.js
- [ ] T032 [US2] Create token management service in react-ui/src/services/AuthService.js
- [ ] T033 [US2] Add authentication guards to protected routes in react-ui/src/guards/AuthGuard.js

**Checkpoint**: Customers can register, login, and access only their own cart/order data

---

## Phase 5: User Story 5 - Password Reset and Account Recovery (Priority: P2)

**Goal**: Users can reset passwords securely without support intervention

**Independent Test**: Request password reset, receive token, successfully change password, verify old sessions invalidated

### Implementation for User Story 5

- [ ] T034 [P] [US5] Create PasswordResetToken entity in login-microservice/src/main/java/entities/PasswordResetToken.java
- [ ] T035 [P] [US5] Create PasswordResetService in login-microservice/src/main/java/services/PasswordResetService.java
- [ ] T036 [US5] Create password reset request endpoint in login-microservice/src/main/java/controllers/AuthController.java
- [ ] T037 [US5] Create password reset confirmation endpoint in login-microservice/src/main/java/controllers/AuthController.java
- [ ] T038 [US5] Create password reset form component in react-ui/src/auth/PasswordResetForm.js
- [ ] T039 [US5] Create password reset confirmation component in react-ui/src/auth/PasswordResetConfirmation.js
- [ ] T040 [US5] Add password reset navigation to login form in react-ui/src/auth/LoginForm.js

**Checkpoint**: Password reset workflow complete and functional

---

## Phase 6: User Story 3 - Support Team Customer Assistance (Priority: P2)

**Goal**: Support users can view customer data read-only for assistance

**Independent Test**: Support user logs in, searches customer accounts, views cart/orders read-only, cannot modify data

### Implementation for User Story 3

- [ ] T041 [P] [US3] Add ROLE_SUPPORT to role definitions in login-microservice/src/main/java/entities/Role.java
- [ ] T042 [P] [US3] Create support role authorization in cart-microservice/src/main/java/security/SupportAccessService.java
- [ ] T043 [P] [US3] Create support role authorization in checkout-microservice/src/main/java/security/SupportAccessService.java
- [ ] T044 [US3] Add customer search endpoint for support in login-microservice/src/main/java/controllers/SupportController.java
- [ ] T045 [US3] Add read-only cart access for support role in cart-microservice/src/main/java/controllers/CartController.java
- [ ] T046 [US3] Add read-only order access for support role in checkout-microservice/src/main/java/controllers/OrderController.java
- [ ] T047 [US3] Create support dashboard component in react-ui/src/components/Support/SupportDashboard.js
- [ ] T048 [US3] Create customer search component in react-ui/src/components/Support/CustomerSearch.js
- [ ] T049 [US3] Add support role routing in react-ui/src/guards/RoleGuard.js

**Checkpoint**: Support team can view customer data read-only for assistance

---

## Phase 7: User Story 4 - Administrative System Management (Priority: P3)

**Goal**: Admins can manage products, inventory, users, and system operations with audit logging

**Independent Test**: Admin user manages products, assigns roles, views audit logs, all actions properly logged

### Implementation for User Story 4

- [ ] T050 [P] [US4] Add ROLE_ADMIN to role definitions in login-microservice/src/main/java/entities/Role.java
- [ ] T051 [P] [US4] Create AuditLog entity in login-microservice/src/main/java/entities/AuditLog.java
- [ ] T052 [P] [US4] Create AuditService for logging admin actions in login-microservice/src/main/java/services/AuditService.java
- [ ] T053 [US4] Add admin authorization to product management in products-microservice/src/main/java/security/AdminSecurityConfig.java
- [ ] T054 [US4] Create admin product management endpoints in products-microservice/src/main/java/controllers/AdminProductController.java
- [ ] T055 [US4] Create user role management endpoints in login-microservice/src/main/java/controllers/AdminUserController.java
- [ ] T056 [US4] Add audit logging to all admin operations across all services
- [ ] T057 [US4] Create admin dashboard component in react-ui/src/components/Admin/AdminDashboard.js
- [ ] T058 [US4] Create product management interface in react-ui/src/components/Admin/ProductManager.js
- [ ] T059 [US4] Create user management interface in react-ui/src/components/Admin/UserManager.js
- [ ] T060 [US4] Create audit log viewer in react-ui/src/components/Admin/AuditLogViewer.js

**Checkpoint**: Full administrative capabilities with complete audit trails

---

## Phase 8: Integration Testing & Migration

**Purpose**: End-to-end testing and migration from hardcoded users

- [ ] T061 [P] Create integration tests for complete user registration flow in login-microservice/src/test/java/integration/
- [ ] T062 [P] Create integration tests for role-based access control across all services
- [ ] T063 [P] Create integration tests for data isolation between customers
- [ ] T064 [P] Create end-to-end authentication flow tests in react-ui/tests/
- [ ] T065 Replace hardcoded user "u1001" references with dynamic authentication across all services
- [ ] T066 Create backward compatibility layer to support existing test data during transition
- [ ] T067 Validate security audit requirements are met across all user stories
- [ ] T068 Performance testing for authentication overhead (<100ms requirement)

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T069 [P] Add comprehensive error handling and user-friendly error messages across all services
- [ ] T070 [P] Add security headers and CORS configuration to api-gateway-microservice
- [ ] T071 [P] Create developer documentation for RBAC system in docs/
- [ ] T072 [P] Add monitoring and metrics for authentication success rates
- [ ] T073 [P] Security hardening review of JWT implementation and session management
- [ ] T074 [P] Performance optimization for user context propagation
- [ ] T075 Create quickstart.md validation checklist for complete RBAC system

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 & 2 (P1): Highest priority, can proceed in parallel after Foundation
  - User Story 5 & 3 (P2): Can start after Foundation, parallel with P1 stories
  - User Story 4 (P3): Can start after Foundation, lowest priority
- **Integration Testing (Phase 8)**: Depends on all desired user stories being complete
- **Polish (Phase 9)**: Depends on all previous phases

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - No dependencies on other stories
- **User Story 2 (P1)**: Can start after Foundational - Independent of US1 but integrates with it
- **User Story 5 (P2)**: Depends on User Story 2 (requires authentication system)
- **User Story 3 (P2)**: Depends on User Story 2 (requires role system)
- **User Story 4 (P3)**: Depends on User Story 2 (requires role system and audit framework)

### Within Each User Story

- JWT utilities and base entities before services
- Services before controllers
- Backend implementation before frontend components
- Core functionality before integration features

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes:
  - User Story 1 and 2 can start in parallel (both P1 priority)
  - Models, entities, and components within each story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different AI agents

---

## Implementation Strategy

### MVP First (User Stories 1 & 2 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1 (Anonymous browsing)
4. Complete Phase 4: User Story 2 (Customer auth)
5. **STOP and VALIDATE**: Test core authentication flow independently
6. Deploy/demo if ready

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 + 2 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 5 → Password reset capability → Deploy/Demo
4. Add User Story 3 → Support capabilities → Deploy/Demo
5. Add User Story 4 → Full admin capabilities → Deploy/Demo
6. Each story adds value without breaking previous stories

### Parallel AI Agent Strategy

With multiple AI agents:

1. All agents complete Setup + Foundational together
2. Once Foundational is done:
   - Agent A: User Story 1 (Anonymous browsing)
   - Agent B: User Story 2 (Customer authentication)
   - Agent C: User Story 5 (Password reset) - after Agent B completes auth foundation
   - Agent D: User Story 3 (Support access) - after Agent B completes role system
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies within user story
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- All file paths respect existing microservices structure
- No structural changes to repository layout
- Maintain backward compatibility throughout migration
- Focus on security and data isolation at every step
- Validate authentication flow at each checkpoint