# Tasks: Admin Portal for Product Management

**Input**: Design documents from `/specs/002-admin-portal/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/admin-api.yaml

**Tests**: Tests are NOT explicitly requested in the specification. Test tasks are omitted per template guidelines.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US8)
- Include exact file paths in descriptions

## Path Conventions

Based on plan.md structure:
- **Backend**: `admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/`
- **Frontend**: `react-ui/frontend/src/components/Admin/`
- **Tests**: `admin-microservice/src/test/java/`, `react-ui/frontend/src/__tests__/admin/`
- **Resources**: `admin-microservice/src/main/resources/`, `resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create admin-microservice Maven project structure per plan.md
- [ ] T002 Add Spring Boot 2.6.3 dependencies to admin-microservice/pom.xml (Spring Web, Spring Security, Spring Data JPA, Spring Cloud Eureka Client, YugabyteDB JDBC driver)
- [ ] T003 [P] Create application.yml configuration in admin-microservice/src/main/resources/application.yml (server port 8084, Eureka registration, database connection)
- [ ] T004 [P] Create bootstrap.yml for Eureka configuration in admin-microservice/src/main/resources/bootstrap.yml
- [ ] T005 [P] Create main Spring Boot application class YugastoreAdmin.java in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/YugastoreAdmin.java
- [ ] T006 [P] Create package structure (controller/, service/, repository/, domain/, dto/, config/) in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/
- [ ] T007 [P] Add React Router dependencies to react-ui/frontend/package.json for admin routes
- [ ] T008 [P] Create Admin component directory structure in react-ui/frontend/src/components/Admin/
- [ ] T009 Create YSQL schema script schema-admin.sql in admin-microservice/src/main/resources/schema-admin.sql (admin_users and product_audit_log tables)
- [ ] T010 Create YCQL schema extension script schema-products-v2.cql in resources/schema-products-v2.cql (add is_active, deactivated_at, deactivated_by, version fields)
- [ ] T011 [P] Configure Maven plugins (spring-boot-maven-plugin, jacoco for coverage) in admin-microservice/pom.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T012 Setup YugabyteDB configuration class YugabyteConfig.java in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/config/YugabyteConfig.java (YSQL and YCQL datasource configuration)
- [ ] T013 Create AdminRole enum in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/AdminRole.java (ADMIN, EDITOR, VIEWER)
- [ ] T014 Create AdminUser domain entity in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/AdminUser.java (user_id, username, password_hash, role, email, created_at, last_login)
- [ ] T015 Create AdminUserRepository interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/repository/AdminUserRepository.java (extends JpaRepository, findByUsername method)
- [ ] T016 Implement Spring Security configuration SecurityConfig.java in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/config/SecurityConfig.java (JWT filter, password encoder, endpoint security rules)
- [ ] T017 Create JwtUtil utility class in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/util/JwtUtil.java (generateToken, validateToken, extractUsername, extractRole)
- [ ] T018 Create JwtAuthenticationFilter in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/security/JwtAuthenticationFilter.java (intercept requests, validate JWT, set SecurityContext)
- [ ] T019 Create AdminUserService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminUserService.java (authenticate, loadUserByUsername)
- [ ] T020 Implement AdminUserServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminUserServiceImpl.java (implements AdminUserService, UserDetailsService)
- [ ] T021 Create AdminAuthController in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminAuthController.java (POST /api/admin/auth/login, POST /logout, GET /me)
- [ ] T022 Create LoginRequest DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/LoginRequest.java (username, password fields with validation annotations)
- [ ] T023 Create LoginResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/LoginResponse.java (token, expiresIn, user fields)
- [ ] T024 Create AdminUserDto in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/AdminUserDto.java (userId, username, role, email, createdAt, lastLogin)
- [ ] T025 Create ErrorResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ErrorResponse.java (timestamp, status, error, message, details)
- [ ] T026 Create GlobalExceptionHandler in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/exception/GlobalExceptionHandler.java (@ControllerAdvice for validation errors, authentication failures, not found, etc.)
- [ ] T027 [P] Create ProductMetadata domain entity (extended) in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/ProductMetadata.java (asin, title, category, author, price, description, longDescription, imageUrl, quantity, isActive, deactivatedAt, deactivatedBy, version)
- [ ] T028 [P] Create ProductAuditLog domain entity in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/ProductAuditLog.java (auditId, productAsin, userId, actionType, fieldName, oldValue, newValue, reason, ipAddress, timestamp)
- [ ] T029 [P] Create AuditActionType enum in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/AuditActionType.java (CREATE, UPDATE, DELETE, DEACTIVATE, ACTIVATE, BULK_UPDATE)
- [ ] T030 [P] Create ProductAuditLogRepository interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/repository/ProductAuditLogRepository.java (extends JpaRepository, custom queries for filtering)
- [ ] T031 Create AdminAuditService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminAuditService.java (logAction, getProductHistory, getAuditLogs)
- [ ] T032 Implement AdminAuditServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminAuditServiceImpl.java
- [ ] T033 [P] Create admin route configuration in react-ui/frontend/src/App.js (add /admin routes with PrivateRoute wrapper)
- [ ] T034 [P] Create PrivateRoute component in react-ui/frontend/src/components/Admin/PrivateRoute.js (check JWT token, redirect to /admin/login if not authenticated)
- [ ] T035 [P] Create AdminLayout component in react-ui/frontend/src/components/Admin/Layout/AdminLayout.js (navigation, header, role display)
- [ ] T036 [P] Create AdminNav component in react-ui/frontend/src/components/Admin/Layout/AdminNav.js (sidebar navigation with links to products, audit logs)
- [ ] T037 [P] Create authService.js in react-ui/frontend/src/services/admin/authService.js (login, logout, getCurrentUser, isAuthenticated, getToken methods)
- [ ] T038 [P] Create apiClient.js configuration in react-ui/frontend/src/services/apiClient.js (Axios instance with JWT interceptor, base URL)
- [ ] T039 [P] Create AdminLogin component in react-ui/frontend/src/components/Admin/Auth/AdminLogin.js (login form with username/password, error handling)

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 8 - Role-Based Access Control (Priority: P1) 🎯 MVP Foundation

**Goal**: Implement authentication and role-based authorization so admin users can log in and access features according to their role (Admin, Editor, Viewer)

**Independent Test**: Create test users with each role (admin, editor, viewer), log in with each, and verify that operations are allowed or denied according to role permissions (Admin has full access, Editor can create/update but not delete, Viewer can only view)

### Implementation for User Story 8

- [ ] T040 [P] [US8] Add @PreAuthorize annotations to AdminProductController methods in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java (DELETE requires ADMIN, POST/PUT require ADMIN or EDITOR, GET allows all authenticated)
- [ ] T041 [P] [US8] Add @PreAuthorize annotations to AdminBulkController methods in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminBulkController.java (require ADMIN or EDITOR)
- [ ] T042 [P] [US8] Create AccessDeniedHandler in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/security/CustomAccessDeniedHandler.java (return 403 with clear error message)
- [ ] T043 [P] [US8] Create session timeout interceptor in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/security/SessionTimeoutInterceptor.java (validate JWT expiration, enforce 30-minute timeout)
- [ ] T044 [US8] Update JwtUtil to include 30-minute expiration in token generation in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/util/JwtUtil.java
- [ ] T045 [P] [US8] Create PasswordReset component in react-ui/frontend/src/components/Admin/Auth/PasswordReset.js (form to request password reset email)
- [ ] T046 [US8] Add password reset endpoint POST /api/admin/auth/password-reset to AdminAuthController in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminAuthController.java
- [ ] T047 [US8] Update AdminLayout to display current user role and username in react-ui/frontend/src/components/Admin/Layout/AdminLayout.js
- [ ] T048 [US8] Add role-based UI rendering in AdminNav (hide delete buttons for Editor/Viewer, hide create/edit buttons for Viewer) in react-ui/frontend/src/components/Admin/Layout/AdminNav.js
- [ ] T049 [US8] Implement client-side session timeout detection in authService.js (check token expiration every minute, redirect to login on timeout) in react-ui/frontend/src/services/admin/authService.js
- [ ] T050 [US8] Add logout functionality to AdminLayout with confirmation dialog in react-ui/frontend/src/components/Admin/Layout/AdminLayout.js

**Checkpoint**: At this point, User Story 8 (RBAC) should be fully functional and testable independently. Users can log in, permissions are enforced, and session timeout works.

---

## Phase 4: User Story 3 - Search and Find Products (Priority: P2)

**Goal**: Enable admin users to search for and find products using various criteria (ASIN, name, category, price range, stock status) so they can locate products to manage

**Independent Test**: Create test products with various attributes, then search by ASIN (exact match), partial name, category filter, price range, and stock status. Verify results appear in under 2 seconds, match search criteria, support pagination (25 per page), and sorting by columns.

### Implementation for User Story 3

- [ ] T051 [P] [US3] Create ProductCatalogRestClient Feign interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/repository/ProductCatalogRestClient.java (@FeignClient for products-microservice endpoints)
- [ ] T052 [P] [US3] Create ProductRepository interface for direct YCQL access in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/repository/ProductRepository.java (extends CassandraRepository, custom queries for search/filter)
- [ ] T053 [P] [US3] Create ProductDto in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ProductDto.java (all product fields including isActive, version)
- [ ] T054 [P] [US3] Create ProductListResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ProductListResponse.java (content array, page, size, totalElements, totalPages)
- [ ] T055 [US3] Create AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java (listProducts, searchProducts, getProduct, filterProducts methods)
- [ ] T056 [US3] Implement AdminProductServiceImpl with search/filter logic in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (implements pagination, sorting, filtering by category/price/stock/active status)
- [ ] T057 [US3] Create AdminProductController GET /api/admin/products endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java (query params: page, size, search, category, minPrice, maxPrice, stockStatus, isActive, sortBy, sortOrder)
- [ ] T058 [US3] Create AdminProductController GET /api/admin/products/{asin} endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java
- [ ] T059 [P] [US3] Create productService.js in react-ui/frontend/src/services/admin/productService.js (listProducts, getProduct, searchProducts with query params)
- [ ] T060 [US3] Create ProductList component in react-ui/frontend/src/components/Admin/Products/ProductList.js (table with pagination, displays ASIN, title, price, quantity, category, status)
- [ ] T061 [P] [US3] Create ProductSearch component in react-ui/frontend/src/components/Admin/Products/ProductSearch.js (search input for ASIN or name, triggers search on submit)
- [ ] T062 [P] [US3] Create ProductFilters component in react-ui/frontend/src/components/Admin/Products/ProductFilters.js (dropdowns/inputs for category, price range, stock status, active/inactive)
- [ ] T063 [P] [US3] Create ProductRow component in react-ui/frontend/src/components/Admin/Products/ProductRow.js (table row with product data, action buttons)
- [ ] T064 [P] [US3] Create Pagination component in react-ui/frontend/src/components/Common/Pagination.js (reusable pagination controls, shows page numbers and navigation)
- [ ] T065 [US3] Wire up ProductList with ProductSearch and ProductFilters, implement debounced search in react-ui/frontend/src/components/Admin/Products/ProductList.js
- [ ] T066 [US3] Add sorting functionality to ProductList (click column headers to sort ascending/descending) in react-ui/frontend/src/components/Admin/Products/ProductList.js
- [ ] T067 [US3] Add loading indicator for search operations in react-ui/frontend/src/components/Admin/Products/ProductList.js

**Checkpoint**: At this point, User Story 3 (Search) should be fully functional. Users can search, filter, paginate, and sort products with results appearing quickly.

---

## Phase 5: User Story 1 - Update Product Information (Priority: P1) 🎯 MVP Core

**Goal**: Enable admin users to update product details (price, description, inventory, category, images) quickly so they can respond to market changes without developer support

**Independent Test**: Create a test product, then update each field type (price to 24.99, description text, quantity to 50, category to another valid category, image URL). Verify changes save correctly, validation works (negative price rejected), and concurrent edit detection warns when another user modified the product.

### Implementation for User Story 1

- [ ] T068 [P] [US1] Create ProductUpdateRequest DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ProductUpdateRequest.java (title, category, author, price, description, longDescription, imageUrl, quantity, version with JSR-303 validation annotations)
- [ ] T069 [P] [US1] Create ConcurrencyErrorResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ConcurrencyErrorResponse.java (modifiedBy, modifiedAt, currentVersion)
- [ ] T070 [P] [US1] Create validators utility class in react-ui/frontend/src/utils/validators.js (validatePrice, validateUrl, validateAsin, validateTextLength functions)
- [ ] T071 [P] [US1] Create formatters utility class in react-ui/frontend/src/utils/formatters.js (formatPrice, formatDate functions)
- [ ] T072 [US1] Add updateProduct method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java
- [ ] T073 [US1] Implement updateProduct in AdminProductServiceImpl with optimistic locking in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (check version, update fields, increment version, call ProductCatalogRestClient or direct YCQL update)
- [ ] T074 [US1] Add audit logging to updateProduct (log each field change) in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (call AdminAuditService.logAction for each field changed)
- [ ] T075 [US1] Create AdminProductController PUT /api/admin/products/{asin} endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java (handle 409 Conflict for version mismatch)
- [ ] T076 [US1] Add updateProduct method to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T077 [US1] Create ProductEditForm component in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js (form with all editable fields, ASIN read-only, real-time validation)
- [ ] T078 [P] [US1] Create ProductFormValidation.js in react-ui/frontend/src/components/Admin/ProductForm/ProductFormValidation.js (client-side validation rules matching backend constraints)
- [ ] T079 [US1] Add image URL preview to ProductEditForm in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js (display image thumbnail when URL is valid)
- [ ] T080 [US1] Implement concurrent edit detection in ProductEditForm (handle 409 response, show conflict dialog with modifiedBy/modifiedAt, options to reload or overwrite) in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js
- [ ] T081 [US1] Add success notification after product update in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js
- [ ] T082 [US1] Add error handling and display validation errors inline in ProductEditForm in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js

**Checkpoint**: At this point, User Story 1 (Update Products) should be fully functional. Users can update any product field, validation works, concurrent edits are detected, and audit logs are created.

---

## Phase 6: User Story 2 - Add New Products to Catalog (Priority: P2)

**Goal**: Enable admin users to add new products to the catalog so they can expand product offerings without technical assistance

**Independent Test**: Access "Add Product" form, enter all required fields (ASIN, title, category, price, author) and optional fields (description, image URL, quantity). Verify ASIN uniqueness is enforced (duplicate ASIN rejected), product is created as active by default, and appears immediately in product list and customer catalog.

### Implementation for User Story 2

- [ ] T083 [P] [US2] Create ProductCreateRequest DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/ProductCreateRequest.java (asin, title, category, author, price, description, longDescription, imageUrl, quantity with JSR-303 validation, ASIN required and unique)
- [ ] T084 [US2] Add createProduct method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java
- [ ] T085 [US2] Implement createProduct in AdminProductServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (validate ASIN uniqueness, set isActive=true and version=0, call ProductCatalogRestClient or direct YCQL insert)
- [ ] T086 [US2] Add audit logging to createProduct in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (log CREATE action with full product JSON in newValue)
- [ ] T087 [US2] Create AdminProductController POST /api/admin/products endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java (return 201 Created, handle 400 for duplicate ASIN)
- [ ] T088 [US2] Add createProduct method to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T089 [US2] Create ProductCreateForm component in react-ui/frontend/src/components/Admin/ProductForm/ProductCreateForm.js (form with all required and optional fields, real-time validation)
- [ ] T090 [US2] Add ASIN uniqueness validation in ProductCreateForm (show error if ASIN already exists) in react-ui/frontend/src/components/Admin/ProductForm/ProductCreateForm.js
- [ ] T091 [US2] Add "Add Product" button to ProductList component in react-ui/frontend/src/components/Admin/Products/ProductList.js (navigates to /admin/products/new)
- [ ] T092 [US2] Add route for /admin/products/new in react-ui/frontend/src/App.js (renders ProductCreateForm)
- [ ] T093 [US2] Implement form submission in ProductCreateForm (on success, redirect to product list with success message) in react-ui/frontend/src/components/Admin/ProductForm/ProductCreateForm.js
- [ ] T094 [US2] Add cancel button to ProductCreateForm (returns to product list) in react-ui/frontend/src/components/Admin/ProductForm/ProductCreateForm.js

**Checkpoint**: At this point, User Story 2 (Add Products) should be fully functional. Users can create new products, ASIN uniqueness is enforced, and new products appear immediately in the catalog.

---

## Phase 7: User Story 4 - Deactivate Products (Priority: P3)

**Goal**: Enable admin users to deactivate products (soft delete) without permanently removing them so they can manage product lifecycle while preserving order history

**Independent Test**: Deactivate an active product, verify it no longer appears in customer-facing catalog but shows in admin portal with "Inactive" status. Reactivate the product and verify it returns to customer catalog. Check that historical orders still show deactivated product details.

### Implementation for User Story 4

- [ ] T095 [US4] Add deactivateProduct method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java
- [ ] T096 [US4] Add activateProduct method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java
- [ ] T097 [US4] Implement deactivateProduct in AdminProductServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (set isActive=false, deactivatedAt=NOW, deactivatedBy=currentUserId)
- [ ] T098 [US4] Implement activateProduct in AdminProductServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (set isActive=true, deactivatedAt=null, deactivatedBy=null)
- [ ] T099 [P] [US4] Add audit logging to deactivateProduct in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (log DEACTIVATE action)
- [ ] T100 [P] [US4] Add audit logging to activateProduct in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (log ACTIVATE action)
- [ ] T101 [US4] Create AdminProductController PATCH /api/admin/products/{asin}/deactivate endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java
- [ ] T102 [US4] Create AdminProductController PATCH /api/admin/products/{asin}/activate endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java
- [ ] T103 [P] [US4] Add deactivateProduct method to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T104 [P] [US4] Add activateProduct method to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T105 [US4] Add "Deactivate" button to ProductRow component for active products in react-ui/frontend/src/components/Admin/Products/ProductRow.js (no confirmation required)
- [ ] T106 [US4] Add "Reactivate" button to ProductRow component for inactive products in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T107 [US4] Add "Inactive" status badge to ProductRow for deactivated products in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T108 [US4] Add isActive filter to ProductFilters (show all, active only, inactive only) in react-ui/frontend/src/components/Admin/Products/ProductFilters.js
- [ ] T109 [US4] Update products-microservice ProductCatalogController to filter by isActive=true for customer-facing endpoints in products-microservice/src/main/java/com/yugabyte/app/yugastore/controller/ProductCatalogController.java

**Checkpoint**: At this point, User Story 4 (Deactivate Products) should be fully functional. Users can deactivate/reactivate products, inactive products don't appear to customers, and can filter by status in admin portal.

---

## Phase 8: User Story 5 - Bulk Update Products (Priority: P3)

**Goal**: Enable admin users to update multiple products simultaneously so they can efficiently apply catalog-wide changes

**Independent Test**: Select multiple products via checkboxes, apply bulk operations (category change, price adjustment percentage, deactivation). Verify all selected products are updated, progress indicator shows during operation, and summary report displays successes and failures.

### Implementation for User Story 5

- [ ] T110 [P] [US5] Create BulkUpdateRequest DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/BulkUpdateRequest.java (asins array, operation enum, parameters map)
- [ ] T111 [P] [US5] Create BulkOperationResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/BulkOperationResponse.java (jobId, message, statusUrl)
- [ ] T112 [P] [US5] Create BulkJobStatus DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/BulkJobStatus.java (jobId, status enum, totalProducts, processedProducts, successfulUpdates, failedUpdates, failures array)
- [ ] T113 [P] [US5] Create BulkOperationType enum in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/domain/BulkOperationType.java (UPDATE_CATEGORY, ADJUST_PRICE, DEACTIVATE)
- [ ] T114 [US5] Create AdminBulkService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminBulkService.java (executeBulkUpdate, getBulkJobStatus)
- [ ] T115 [US5] Implement AdminBulkServiceImpl with async processing in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminBulkServiceImpl.java (@Async method, process products one by one, track success/failure, update job status)
- [ ] T116 [US5] Create in-memory job tracker (ConcurrentHashMap) in AdminBulkServiceImpl for storing bulk job status in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminBulkServiceImpl.java
- [ ] T117 [US5] Add audit logging to bulk operations in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminBulkServiceImpl.java (log BULK_UPDATE action with operation description)
- [ ] T118 [US5] Create AdminBulkController POST /api/admin/products/bulk-update endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminBulkController.java (return 202 Accepted with jobId)
- [ ] T119 [US5] Create AdminBulkController POST /api/admin/products/bulk-deactivate endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminBulkController.java
- [ ] T120 [US5] Create AdminBulkController GET /api/admin/jobs/{jobId} endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminBulkController.java (return current job status)
- [ ] T121 [P] [US5] Add bulkUpdate and bulkDeactivate methods to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T122 [P] [US5] Add getJobStatus method to productService.js in react-ui/frontend/src/services/admin/productService.js (poll for bulk job status)
- [ ] T123 [US5] Add checkbox selection to ProductRow component in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T124 [US5] Add select-all checkbox to ProductList component in react-ui/frontend/src/components/Admin/Products/ProductList.js
- [ ] T125 [US5] Add bulk actions toolbar to ProductList (appears when products selected) in react-ui/frontend/src/components/Admin/Products/ProductList.js (buttons for bulk category change, price adjustment, deactivate)
- [ ] T126 [US5] Create BulkUpdateModal component in react-ui/frontend/src/components/Admin/BulkOperations/BulkUpdateModal.js (shows affected product count, operation form, confirmation)
- [ ] T127 [P] [US5] Create BulkProgressIndicator component in react-ui/frontend/src/components/Admin/BulkOperations/BulkProgressIndicator.js (progress bar, shows X/Y products processed, polls job status every 2 seconds)
- [ ] T128 [US5] Wire up BulkUpdateModal to submit bulk operations in react-ui/frontend/src/components/Admin/BulkOperations/BulkUpdateModal.js
- [ ] T129 [US5] Show BulkProgressIndicator after bulk operation submission in react-ui/frontend/src/components/Admin/Products/ProductList.js
- [ ] T130 [US5] Display summary report when bulk operation completes (show successes, failures with reasons) in react-ui/frontend/src/components/Admin/Products/ProductList.js

**Checkpoint**: At this point, User Story 5 (Bulk Updates) should be fully functional. Users can select multiple products, apply bulk operations, see progress, and view summary reports.

---

## Phase 9: User Story 6 - Permanently Delete Products (Priority: P4)

**Goal**: Enable admin users to permanently delete products with safeguards so they can remove erroneous products

**Independent Test**: Attempt to delete products in various states (active - must deactivate first; in cart/pending order - blocked; in completed orders - warning but allowed with reason). Verify confirmation dialog requires typing "DELETE" and reason, audit log records deletion with user and reason.

### Implementation for User Story 6

- [ ] T131 [US6] Add checkProductReferences method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java (check if product in active carts or pending orders)
- [ ] T132 [US6] Implement checkProductReferences in AdminProductServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (call cart-microservice and checkout-microservice via Feign clients)
- [ ] T133 [US6] Add deleteProduct method to AdminProductService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminProductService.java
- [ ] T134 [US6] Implement deleteProduct in AdminProductServiceImpl with safeguards in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (check isActive=false, check references, perform hard delete)
- [ ] T135 [US6] Add audit logging to deleteProduct in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminProductServiceImpl.java (log DELETE action with reason before deletion)
- [ ] T136 [US6] Create AdminProductController DELETE /api/admin/products/{asin} endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminProductController.java (require reason in request body, return 400 if validation fails)
- [ ] T137 [P] [US6] Add deleteProduct method to productService.js in react-ui/frontend/src/services/admin/productService.js
- [ ] T138 [P] [US6] Create ConfirmDialog component in react-ui/frontend/src/components/Common/ConfirmDialog.js (reusable modal for confirmations with custom title, message, actions)
- [ ] T139 [US6] Add "Delete Permanently" button to ProductRow component (only visible for Admin role, only enabled for inactive products) in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T140 [US6] Create DeleteConfirmationDialog for permanent deletion in react-ui/frontend/src/components/Admin/Products/ProductRow.js (requires typing "DELETE", reason dropdown + text field, shows warnings)
- [ ] T141 [US6] Handle deletion errors in ProductRow (show error messages for active products, products in carts, products in pending orders) in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T142 [US6] Show warning for products in completed orders (allow deletion but highlight the warning) in react-ui/frontend/src/components/Admin/Products/ProductRow.js
- [ ] T143 [US6] Refresh product list after successful deletion in react-ui/frontend/src/components/Admin/Products/ProductList.js

**Checkpoint**: At this point, User Story 6 (Permanent Delete) should be fully functional. Users can permanently delete inactive products with proper safeguards, confirmation, and reason tracking.

---

## Phase 10: User Story 7 - View Product Change History (Priority: P4)

**Goal**: Enable admin users to view product change history so they can audit changes and troubleshoot issues

**Independent Test**: Make various changes to a test product (update price, change category, update description). View the product's audit history and verify each change is recorded with field name, old value, new value, user who made the change, and timestamp. Filter by date range and user.

### Implementation for User Story 7

- [ ] T144 [P] [US7] Create AuditLogDto in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/AuditLogDto.java (auditId, productAsin, userId, username, actionType, fieldName, oldValue, newValue, reason, ipAddress, timestamp)
- [ ] T145 [P] [US7] Create AuditLogListResponse DTO in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/dto/AuditLogListResponse.java (content array, page, size, totalElements, totalPages)
- [ ] T146 [US7] Add getProductAuditHistory method to AdminAuditService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminAuditService.java (filter by product ASIN, pagination)
- [ ] T147 [US7] Add getAuditLogs method to AdminAuditService interface in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/AdminAuditService.java (filter by userId, actionType, fromDate, toDate, productAsin, pagination)
- [ ] T148 [US7] Implement getProductAuditHistory in AdminAuditServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminAuditServiceImpl.java (query ProductAuditLogRepository ordered by timestamp DESC)
- [ ] T149 [US7] Implement getAuditLogs in AdminAuditServiceImpl in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminAuditServiceImpl.java (build query with filters, join with AdminUser for username)
- [ ] T150 [US7] Create AdminAuditController GET /api/admin/audit/products/{asin} endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminAuditController.java (pagination params)
- [ ] T151 [US7] Create AdminAuditController GET /api/admin/audit/logs endpoint in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/controller/AdminAuditController.java (filter params: userId, actionType, fromDate, toDate, productAsin, pagination)
- [ ] T152 [P] [US7] Create auditService.js in react-ui/frontend/src/services/admin/auditService.js (getProductHistory, getAuditLogs with filter params)
- [ ] T153 [US7] Create AuditLogViewer component in react-ui/frontend/src/components/Admin/AuditLog/AuditLogViewer.js (table showing audit entries with field changes, user, timestamp)
- [ ] T154 [P] [US7] Create AuditLogFilters component in react-ui/frontend/src/components/Admin/AuditLog/AuditLogFilters.js (filters for user, action type, date range, product ASIN)
- [ ] T155 [US7] Add route for /admin/audit in react-ui/frontend/src/App.js (renders AuditLogViewer)
- [ ] T156 [US7] Add "View History" button to ProductEditForm in react-ui/frontend/src/components/Admin/ProductForm/ProductEditForm.js (shows product-specific audit history in modal)
- [ ] T157 [US7] Add audit log navigation link to AdminNav in react-ui/frontend/src/components/Admin/Layout/AdminNav.js
- [ ] T158 [US7] Implement audit log retention (scheduled job to delete logs older than 90 days) in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/service/impl/AdminAuditServiceImpl.java (@Scheduled method)

**Checkpoint**: At this point, User Story 7 (Audit History) should be fully functional. Users can view product change history, filter audit logs, and see who made what changes and when.

---

## Phase 11: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T159 [P] Add API Gateway routes for /api/admin/** in api-gateway-microservice/src/main/java/com/yugabyte/app/yugastore/config/GatewayRoutes.java (route to admin-microservice)
- [ ] T160 [P] Configure CORS for admin-microservice in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/config/SecurityConfig.java (allow react-ui origin)
- [ ] T161 [P] Add Swagger/OpenAPI documentation to admin-microservice in admin-microservice/pom.xml (springdoc-openapi dependency) and admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/YugastoreAdmin.java (@OpenAPIDefinition)
- [ ] T162 [P] Add actuator health endpoint to admin-microservice in admin-microservice/pom.xml (spring-boot-starter-actuator)
- [ ] T163 [P] Configure structured logging with correlation IDs in admin-microservice/src/main/resources/logback-spring.xml
- [ ] T164 [P] Add rate limiting configuration (100 requests/minute per user) in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/config/RateLimitConfig.java
- [ ] T165 [P] Add CSRF protection configuration in admin-microservice/src/main/java/com/yugabyte/app/yugastore/admin/config/SecurityConfig.java
- [ ] T166 [P] Create loading spinner component in react-ui/frontend/src/components/Common/LoadingSpinner.js (reusable for all async operations)
- [ ] T167 [P] Create toast notification system in react-ui/frontend/src/components/Common/Toast.js (success, error, warning notifications)
- [ ] T168 [P] Add error boundary component in react-ui/frontend/src/components/Common/ErrorBoundary.js (catch React errors gracefully)
- [ ] T169 [P] Implement responsive design for mobile/tablet in admin UI components (add media queries, test on different screen sizes)
- [ ] T170 [P] Add keyboard navigation support to ProductList and forms (tab order, enter to submit, escape to cancel)
- [ ] T171 [P] Add ARIA labels and roles for screen reader accessibility across all admin components
- [ ] T172 [P] Create README.md for admin-microservice with setup instructions in admin-microservice/README.md
- [ ] T173 [P] Update main README.md with admin portal section in README.md
- [ ] T174 Run quickstart.md validation (follow all setup steps, verify all endpoints work)
- [ ] T175 Run security audit (check for SQL injection, XSS, CSRF vulnerabilities)
- [ ] T176 [P] Add performance monitoring (log slow queries > 2 seconds) in admin-microservice
- [ ] T177 [P] Create database indexes for frequent queries in schema-admin.sql (idx_audit_product, idx_audit_user, idx_audit_timestamp)
- [ ] T178 Code cleanup and remove console.log statements from React components
- [ ] T179 Run linter and fix warnings across backend and frontend
- [ ] T180 Create initial admin users via seed script in resources/seed-admin-users.sql

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Story 8 - RBAC (Phase 3)**: Depends on Foundational - MVP foundation, should complete before other stories
- **User Story 3 - Search (Phase 4)**: Depends on Foundational - Independent of other user stories
- **User Story 1 - Update (Phase 5)**: Depends on Foundational and US3 (search to find products to edit) - Core MVP functionality
- **User Story 2 - Add Products (Phase 6)**: Depends on Foundational and US3 - Independent of US1/US4/US5/US6/US7
- **User Story 4 - Deactivate (Phase 7)**: Depends on Foundational and US3 - Independent of US1/US2/US5/US6/US7
- **User Story 5 - Bulk Updates (Phase 8)**: Depends on Foundational, US3, and US1 (reuses update logic) - Can run in parallel with US4/US6/US7
- **User Story 6 - Delete (Phase 9)**: Depends on Foundational, US3, and US4 (must deactivate first) - Can run in parallel with US5/US7
- **User Story 7 - Audit History (Phase 10)**: Depends on Foundational (audit logs created throughout) - Can run in parallel with any story
- **Polish (Phase 11)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 8 (RBAC - P1)**: No dependencies on other user stories - Should complete first as it's foundational for security
- **User Story 3 (Search - P2)**: No dependencies on other user stories - Needed by most other stories to find products
- **User Story 1 (Update - P1)**: Depends on US3 (search) to find products to edit - Core value delivery
- **User Story 2 (Add - P2)**: Depends on US3 (to see new products in list) - Independent otherwise
- **User Story 4 (Deactivate - P3)**: Depends on US3 - Independent of US1/US2/US5/US6/US7
- **User Story 5 (Bulk - P3)**: Depends on US3 and reuses US1 update logic - Independent of US4/US6/US7
- **User Story 6 (Delete - P4)**: Depends on US3 and US4 (must deactivate before delete) - Independent of US1/US2/US5/US7
- **User Story 7 (Audit - P4)**: No blocking dependencies (audit logs created by all operations) - Can implement anytime

### Recommended Implementation Order

**Priority 1 - MVP (Phases 1-5):**
1. Setup → Foundational → US8 (RBAC) → US3 (Search) → US1 (Update Products)
2. This delivers the core value: authenticated users can search for and update products
3. **Stop and validate**: Test MVP independently before proceeding

**Priority 2 (Phase 6):**
4. US2 (Add Products) - Enables catalog growth

**Priority 3 (Phases 7-10):**
5. US4, US5, US6, US7 can be done in any order based on business priority
6. These can be implemented in parallel by multiple agents

### Within Each User Story

- Models/Entities before services
- Services before controllers
- Controllers/API before frontend components
- Core implementation before integrations
- Story complete and tested before moving to next

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes:
  - US8 should complete first (security foundation)
  - US3 should complete next (needed by others)
  - After US3: US2, US4, US7 can run in parallel (independent)
  - After US1: US5 can run in parallel with US4/US6/US7
  - After US4: US6 can run in parallel with US5/US7
- Within each story: Models marked [P] can run in parallel, DTOs marked [P] can run in parallel, frontend components marked [P] can run in parallel
- Polish tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1 (Update Products)

```bash
# After US3 (Search) completes, launch these US1 tasks in parallel:

# Parallel DTOs:
Task: "Create ProductUpdateRequest DTO in admin-microservice/src/main/java/.../dto/ProductUpdateRequest.java"
Task: "Create ConcurrencyErrorResponse DTO in admin-microservice/src/main/java/.../dto/ConcurrencyErrorResponse.java"

# Parallel utilities (different files):
Task: "Create validators utility class in react-ui/frontend/src/utils/validators.js"
Task: "Create formatters utility class in react-ui/frontend/src/utils/formatters.js"

# Then sequentially:
Task: "Add updateProduct method to AdminProductService interface"
Task: "Implement updateProduct in AdminProductServiceImpl with optimistic locking"
Task: "Add audit logging to updateProduct"
Task: "Create AdminProductController PUT endpoint"

# Parallel frontend:
Task: "Add updateProduct method to productService.js"
Task: "Create ProductFormValidation.js"

# Then:
Task: "Create ProductEditForm component"
Task: "Add image preview, concurrent edit detection, success notification"
```

---

## Implementation Strategy

### MVP First (User Stories 8, 3, 1 Only) 🎯

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 8 (RBAC) - Security foundation
4. Complete Phase 4: User Story 3 (Search) - Find products
5. Complete Phase 5: User Story 1 (Update Products) - Core value
6. **STOP and VALIDATE**: Test authentication, search, and product updates independently
7. Deploy/demo if ready - This is a functional MVP!

**MVP delivers**: Authenticated admin users can search for products and update product information quickly, eliminating developer dependency for routine changes. This addresses the primary business pain point.

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add US8 (RBAC) → Test authentication and permissions → Foundation validated
3. Add US3 (Search) → Test search/filter/pagination → Search works
4. Add US1 (Update) → Test product updates → **Deploy/Demo MVP!** 🎯
5. Add US2 (Add Products) → Test product creation → Deploy/Demo (catalog growth enabled)
6. Add US4 (Deactivate) → Test lifecycle management → Deploy/Demo
7. Add US5 (Bulk Updates) → Test bulk operations → Deploy/Demo (efficiency gains)
8. Add US6 (Delete) → Test permanent deletion → Deploy/Demo
9. Add US7 (Audit History) → Test audit trails → Deploy/Demo (compliance complete)
10. Polish phase → Security hardening, documentation, performance tuning

Each story adds incremental value without breaking previous stories.

### Parallel Team Strategy

With multiple agents/developers after Foundational phase:

**Foundation Phase**
- Team completes Setup + Foundational together
- Team completes US8 (RBAC) together - Critical security foundation

**Core Features Phase**
- Agent A: US3 (Search) - Blocks other features
- Agent B: Prepare for US1 (review contracts, plan implementation)

**MVP Phase**
- Agent A: US1 (Update Products)
- Agent B: US2 (Add Products)
- **Milestone**: MVP ready (US8 + US3 + US1)

**Additional Features Phase**
- Agent A: US5 (Bulk Updates)
- Agent B: US4 (Deactivate Products)
- Agent C: US7 (Audit History)

**Final Features Phase**
- Agent A: US6 (Permanent Delete)
- Agent B/C: Polish phase

Stories complete and integrate independently.

---

## Summary

- **Total Tasks**: 180 tasks
- **Task Breakdown**:
  - Setup (Phase 1): 11 tasks
  - Foundational (Phase 2): 28 tasks
  - User Story 8 - RBAC (P1): 11 tasks
  - User Story 3 - Search (P2): 17 tasks
  - User Story 1 - Update (P1): 15 tasks
  - User Story 2 - Add Products (P2): 12 tasks
  - User Story 4 - Deactivate (P3): 15 tasks
  - User Story 5 - Bulk Updates (P3): 21 tasks
  - User Story 6 - Delete (P4): 13 tasks
  - User Story 7 - Audit History (P4): 15 tasks
  - Polish: 22 tasks

- **Parallel Opportunities**: 75+ tasks marked [P] can run in parallel (within their phase constraints)

- **Independent Test Criteria**: Each user story has explicit "Independent Test" description showing how to verify it works standalone

- **MVP Scope**: User Stories 8 (RBAC), 3 (Search), and 1 (Update Products) = 71 tasks (Setup + Foundational + US8 + US3 + US1)
  - Delivers core value: Authenticated admin users can quickly search and update products
  - Addresses primary business pain point from spec.md
  - Fully functional and deployable

- **Format Validation**: ✅ ALL tasks follow required checklist format with checkbox, ID, [P] markers, [Story] labels, and file paths

---

## Notes

- [P] tasks = different files, no dependencies within phase
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Tests are NOT included per specification (not explicitly requested)
- All file paths are absolute or relative to repository root
- Backend uses Spring Boot + YugabyteDB (YSQL for admin, YCQL for products)
- Frontend integrates into existing react-ui under /admin routes
- Audit logging is automatic (logged in each service method that modifies data)
