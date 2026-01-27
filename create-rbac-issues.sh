#!/bin/bash

# Script to create RBAC EPIC and User Stories in GitHub
# Repository: hieuctrac/yugastore-java

set -e  # Exit on error

REPO="hieuctrac/yugastore-java"

echo "=========================================="
echo "Creating RBAC EPIC and User Stories"
echo "Repository: $REPO"
echo "=========================================="
echo ""

# Check if gh CLI is authenticated
echo "Checking GitHub CLI authentication..."
if ! gh auth status &> /dev/null; then
    echo "Error: GitHub CLI is not authenticated."
    echo "Please run: gh auth login"
    exit 1
fi
echo "✓ Authenticated"
echo ""

# Create the EPIC issue
echo "Creating EPIC issue..."
EPIC_URL=$(gh issue create \
  --repo "$REPO" \
  --title "🔐 EPIC: Implement Role-Based Access Control (RBAC)" \
  --body "## Epic Overview

Implement comprehensive Role-Based Access Control (RBAC) across the YugaStore platform to ensure different user types (customers, administrators, support staff, and anonymous users) have appropriate access to system features and data, ensuring security and compliance with the principle of least privilege.

### Business Value

- Protect customer data and prevent unauthorized access
- Enable multi-tenant operations with proper data isolation
- Support different user personas with appropriate capabilities
- Meet compliance requirements (GDPR, PCI-DSS)
- Provide accountability and audit trails
- Make the application production-ready

### Current State

- No authorization mechanism exists
- All API endpoints are publicly accessible
- Fixed user ID (\"u1001\") hardcoded across services
- Role entities defined but not utilized
- Login microservice exists but not integrated

### Target Roles

1. **ROLE_ANONYMOUS** - Unauthenticated users (browse only)
2. **ROLE_CUSTOMER** - Registered shoppers
3. **ROLE_SUPPORT** - Customer service representatives
4. **ROLE_ADMIN** - System administrators

### Success Metrics

- Zero unauthorized access attempts succeed
- All endpoints properly protected
- User context correctly propagated across services
- Role-based tests achieve 100% pass rate

---

## User Stories

This epic is broken down into 9 user stories totaling 36 story points.

See linked issues below for individual user stories.")

# Extract issue number from URL
EPIC_NUMBER=$(echo "$EPIC_URL" | grep -o '[0-9]*$')

if [ -z "$EPIC_NUMBER" ]; then
    echo "Error: Failed to create EPIC or extract issue number"
    exit 1
fi

echo "✓ EPIC created: #$EPIC_NUMBER"
echo "  URL: $EPIC_URL"
echo ""

# Create US-1: JWT-Based Authentication Infrastructure
echo "Creating US-1: JWT-Based Authentication Infrastructure..."
US1_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-1: JWT-Based Authentication Infrastructure" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH (Foundational)
**Story Points:** 5

## User Story

**As a** platform security engineer  
**I want to** implement JWT-based authentication in the login microservice  
**So that** users can securely authenticate and receive tokens with embedded role information

## Description

Update the login microservice to issue JWT tokens upon successful authentication. The tokens should include user ID, username, roles, and expiration time. This provides the foundation for stateless authentication across all microservices.

## Acceptance Criteria

- [ ] Login endpoint (\`POST /login\`) returns JWT token on successful authentication
- [ ] JWT payload includes: \`userId\`, \`username\`, \`roles[]\`, \`iat\` (issued at), \`exp\` (expiration)
- [ ] Token expiration set to 24 hours (configurable)
- [ ] Refresh token endpoint (\`POST /refresh\`) allows token renewal
- [ ] JWT signed with secure secret key (HS256 or RS256)
- [ ] Token validation utility method available for other services
- [ ] Invalid credentials return 401 with clear error message
- [ ] Logout endpoint (\`POST /logout\`) invalidates token (if using token blacklist)

## Technical Notes

**Dependencies:**
\`\`\`xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
\`\`\`

**JWT Structure:**
\`\`\`json
{
  \"sub\": \"john.doe\",
  \"userId\": \"12345\",
  \"roles\": [\"ROLE_CUSTOMER\"],
  \"iat\": 1643270400,
  \"exp\": 1643356800
}
\`\`\`

**Configuration:**
- Secret key stored in environment variable or secure vault
- Token expiration configurable via \`application.yml\`

## Testing

- Unit tests for JWT generation and validation
- Test token expiration handling
- Test invalid signature detection
- Test malformed token handling

## Dependencies

- None (foundational story)

## Definition of Done

- [ ] JWT generation working on successful login
- [ ] Token includes all required claims
- [ ] Token validation utility tested
- [ ] Documentation updated with JWT structure
- [ ] Code reviewed and merged")

echo "✓ US-1 created: $(echo "$US1_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-2: Role Management and Assignment
echo "Creating US-2: Role Management and Assignment..."
US2_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-2: Role Management and Assignment" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH (Foundational)
**Story Points:** 3

## User Story

**As a** system administrator  
**I want to** manage user roles and assignments  
**So that** users can be granted appropriate access levels based on their responsibilities

## Description

Implement functionality to assign and manage roles for users. Set up predefined roles (CUSTOMER, SUPPORT, ADMIN, ANONYMOUS) and create admin endpoints to manage user-role assignments.

## Acceptance Criteria

- [ ] Database tables \`username\` and \`role\` properly configured with many-to-many relationship
- [ ] Four predefined roles seeded in database: \`ROLE_CUSTOMER\`, \`ROLE_SUPPORT\`, \`ROLE_ADMIN\`, \`ROLE_ANONYMOUS\`
- [ ] Users assigned \`ROLE_CUSTOMER\` by default on registration
- [ ] Admin endpoint \`PUT /users/{userId}/roles\` to assign roles (requires ROLE_ADMIN)
- [ ] Admin endpoint \`GET /users/{userId}/roles\` to view user roles (requires ROLE_ADMIN)
- [ ] Endpoint \`GET /users\` lists all users with their roles (requires ROLE_ADMIN)
- [ ] Users can have multiple roles simultaneously
- [ ] Role assignments persisted correctly in database

## Technical Notes

**Database Schema:**
\`\`\`sql
-- Ensure tables exist
CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS username (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES username(id),
    FOREIGN KEY (role_id) REFERENCES role(id)
);

-- Seed predefined roles
INSERT INTO role (name) VALUES 
    ('ROLE_ANONYMOUS'),
    ('ROLE_CUSTOMER'),
    ('ROLE_SUPPORT'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;
\`\`\`

## Testing

- Test default role assignment on registration
- Test admin can assign/remove roles
- Test non-admin cannot access role management endpoints
- Test multiple role assignments

## Dependencies

- US-1 (JWT authentication) must be complete for endpoint protection

## Definition of Done

- [ ] Roles seeded in database
- [ ] Role assignment endpoints working
- [ ] Many-to-many relationship functional
- [ ] Tests passing
- [ ] Documentation updated")

echo "✓ US-2 created: $(echo "$US2_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-3: API Gateway Authentication
echo "Creating US-3: API Gateway Authentication and Authorization..."
US3_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-3: API Gateway Authentication and Authorization" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH (Blocking)
**Story Points:** 5

## User Story

**As a** platform architect  
**I want to** validate authentication and extract user context in the API Gateway  
**So that** all requests are authenticated before reaching backend services and user information is available downstream

## Description

Implement JWT validation filter in the API Gateway that intercepts all requests, validates JWT tokens, extracts user information, and forwards user context to backend services via HTTP headers.

## Acceptance Criteria

- [ ] JWT validation filter intercepts all requests except \`/login\`, \`/register\`, and public product browsing endpoints
- [ ] Valid JWT tokens are parsed and validated (signature, expiration)
- [ ] User ID and roles extracted from JWT and added to request headers:
  - \`X-User-Id: {userId}\`
  - \`X-User-Roles: ROLE_CUSTOMER,ROLE_ADMIN\`
  - \`X-Username: {username}\`
- [ ] Invalid/expired tokens return 401 Unauthorized with error message
- [ ] Missing authentication for protected endpoints returns 401
- [ ] Public endpoints (GET \`/products/*\`) accessible without authentication
- [ ] Filter logs authentication failures for security monitoring
- [ ] CORS configuration allows credentials

## Technical Notes

**Public Endpoints:**
- \`GET /products/**\`
- \`GET /products/*/recommendations\`
- \`POST /login\`
- \`POST /register\`

## Testing

- Test valid JWT passes through with correct headers
- Test invalid JWT returns 401
- Test expired JWT returns 401
- Test missing JWT on protected endpoint returns 401
- Test public endpoints accessible without JWT
- Integration test with backend services receiving headers

## Dependencies

- US-1 (JWT authentication) must be complete

## Definition of Done

- [ ] JWT filter implemented and tested
- [ ] User context headers forwarded
- [ ] Public endpoints accessible
- [ ] Protected endpoints blocked without auth
- [ ] Integration tests passing
- [ ] Code reviewed and merged")

echo "✓ US-3 created: $(echo "$US3_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-4: Secure Products Microservice
echo "Creating US-4: Secure Products Microservice with RBAC..."
US4_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-4: Secure Products Microservice with RBAC" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🟡 MEDIUM
**Story Points:** 3

## User Story

**As a** product catalog owner  
**I want to** restrict product and inventory management to authorized users  
**So that** only administrators can modify the catalog while customers and anonymous users can browse

## Description

Implement role-based authorization in the Products microservice, allowing all users to view products but restricting create/update/delete operations to administrators only.

## Acceptance Criteria

- [ ] Spring Security configured in products-microservice
- [ ] GET endpoints (view products) accessible to all authenticated users + anonymous:
  - \`GET /products\`
  - \`GET /products/{asin}\`
  - \`GET /products/{asin}/recommendations\`
  - \`GET /products/category/{category}\`
- [ ] POST/PUT/DELETE endpoints require \`ROLE_ADMIN\`:
  - \`POST /products\` - Create product
  - \`PUT /products/{asin}\` - Update product
  - \`DELETE /products/{asin}\` - Delete product
- [ ] Inventory endpoints:
  - \`GET /inventory\` - Requires \`ROLE_SUPPORT\` or \`ROLE_ADMIN\`
  - \`GET /inventory/{asin}\` - Requires \`ROLE_SUPPORT\` or \`ROLE_ADMIN\`
  - \`PUT /inventory/{asin}\` - Requires \`ROLE_ADMIN\` only
- [ ] Unauthorized access returns 403 Forbidden
- [ ] User roles extracted from \`X-User-Roles\` header
- [ ] Authorization logged for audit purposes

## Testing

- Test anonymous can view products
- Test customer can view products
- Test customer cannot create/update/delete products
- Test admin can perform all operations
- Test support can view inventory
- Test support cannot modify inventory

## Dependencies

- US-3 (API Gateway authentication) must be complete

## Definition of Done

- [ ] Authorization annotations applied to all endpoints
- [ ] Tests passing for all role scenarios
- [ ] 403 errors returned for unauthorized access
- [ ] Documentation updated
- [ ] Code reviewed and merged")

echo "✓ US-4 created: $(echo "$US4_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-5: Secure Cart Microservice
echo "Creating US-5: Secure Cart Microservice with RBAC and User Isolation..."
US5_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-5: Secure Cart Microservice with RBAC and User Isolation" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH
**Story Points:** 5

## User Story

**As a** customer  
**I want to** access only my own shopping cart  
**So that** my cart data is private and other users cannot view or modify my items

## Description

Implement role-based authorization and user data isolation in the Cart microservice. Ensure customers can only access their own carts, while admins can access any cart for support purposes.

## Acceptance Criteria

- [ ] Spring Security configured in cart-microservice
- [ ] User ID extracted from \`X-User-Id\` header provided by API Gateway
- [ ] Cart endpoints for customer access (own cart only):
  - \`GET /cart\` - Returns authenticated user's cart
  - \`POST /cart/items\` - Adds item to authenticated user's cart
  - \`DELETE /cart/items/{asin}\` - Removes item from authenticated user's cart
  - Requires \`ROLE_CUSTOMER\` or \`ROLE_ADMIN\`
- [ ] Admin-only endpoints (any user's cart):
  - \`GET /cart/user/{userId}\` - View any user's cart
  - Requires \`ROLE_ADMIN\`
- [ ] Hardcoded user ID \"1\" removed from all cart operations
- [ ] Cart operations use user ID from security context
- [ ] Customers attempting to access another user's cart receive 403 Forbidden
- [ ] Database queries filter by authenticated user ID

## Testing

- Test customer can access own cart
- Test customer cannot access another user's cart
- Test admin can access any user's cart via admin endpoint
- Test unauthenticated user cannot access cart
- Test cart operations correctly use authenticated user ID

## Dependencies

- US-3 (API Gateway authentication) must be complete
- US-7 (Remove hardcoded user IDs) can be done in parallel

## Definition of Done

- [ ] User isolation enforced
- [ ] Hardcoded user IDs removed
- [ ] Authorization checks in place
- [ ] Tests passing for all scenarios
- [ ] Security review completed
- [ ] Code reviewed and merged")

echo "✓ US-5 created: $(echo "$US5_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-6: Secure Checkout Microservice
echo "Creating US-6: Secure Checkout Microservice with RBAC and Order Isolation..."
US6_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-6: Secure Checkout Microservice with RBAC and Order Isolation" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH
**Story Points:** 5

## User Story

**As a** customer  
**I want to** place orders and view only my own order history  
**So that** my purchase data remains private and secure

## Description

Implement role-based authorization and order data isolation in the Checkout microservice. Customers can only place orders for their own cart and view their own orders. Support staff can view all orders for customer service. Admins have full access.

## Acceptance Criteria

- [ ] Spring Security configured in checkout-microservice
- [ ] User ID extracted from \`X-User-Id\` header
- [ ] Checkout endpoint:
  - \`POST /checkout\` - Place order for authenticated user's cart
  - Requires \`ROLE_CUSTOMER\` or \`ROLE_ADMIN\`
  - Uses authenticated user ID, not hardcoded value
- [ ] Order viewing endpoints:
  - \`GET /orders\` - Customer sees only their orders, Support/Admin see all
  - \`GET /orders/{orderId}\` - Customer sees only if it's their order, Support/Admin see any
  - Requires \`ROLE_CUSTOMER\`, \`ROLE_SUPPORT\`, or \`ROLE_ADMIN\`
- [ ] Order management endpoints:
  - \`PUT /orders/{orderId}\` - Modify order (Admin only)
  - \`DELETE /orders/{orderId}\` - Cancel order (Admin only)
  - Requires \`ROLE_ADMIN\`
- [ ] Inventory endpoints:
  - \`GET /inventory\` - Requires \`ROLE_SUPPORT\` or \`ROLE_ADMIN\`
  - \`PUT /inventory/{asin}\` - Requires \`ROLE_ADMIN\`
- [ ] Hardcoded user ID removed from checkout process
- [ ] Order records store actual user ID from authentication context
- [ ] Customers attempting to view another user's order receive 403 Forbidden

## Testing

- Test customer can place order
- Test customer sees only their orders in order list
- Test customer cannot view another customer's order
- Test support can view all orders
- Test admin can view and modify any order
- Test checkout uses authenticated user ID

## Dependencies

- US-3 (API Gateway authentication) must be complete
- US-5 (Cart isolation) should be complete for consistent user experience

## Definition of Done

- [ ] Order isolation enforced
- [ ] Hardcoded user IDs removed
- [ ] Authorization checks in place
- [ ] Tests passing for all scenarios
- [ ] Security review completed
- [ ] Code reviewed and merged")

echo "✓ US-6 created: $(echo "$US6_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-7: Remove Hardcoded User IDs
echo "Creating US-7: Remove Hardcoded User IDs and Implement User Context..."
US7_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-7: Remove Hardcoded User IDs and Implement User Context" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH
**Story Points:** 3

## User Story

**As a** software engineer  
**I want to** remove all hardcoded user IDs from the codebase  
**So that** services use actual authenticated user context instead of fixed test values

## Description

Systematically find and remove hardcoded user IDs (\"1\", \"u1001\") from all microservices. Replace with authenticated user context extracted from request headers set by the API Gateway.

## Acceptance Criteria

- [ ] All instances of hardcoded user ID \"1\" removed from cart-microservice
- [ ] All instances of hardcoded user ID \"u1001\" removed from all services
- [ ] Services extract user ID from \`X-User-Id\` header
- [ ] Utility class/helper method created for consistent user context extraction
- [ ] Error handling for missing user context (should not happen with proper gateway setup)
- [ ] No default fallback to hardcoded values
- [ ] Database queries use dynamic user ID parameter
- [ ] All tests updated to mock authenticated user context

## Technical Notes

**Files to Update:**
- \`cart-microservice/src/main/java/**\` - Search for hardcoded \"1\"
- \`checkout-microservice/src/main/java/**\` - Search for \"u1001\"
- Any controller or service class performing user-specific operations

**Utility Helper:**
\`\`\`java
@Component
public class UserContextUtil {
    
    public static String getUserId(HttpServletRequest request) {
        String userId = request.getHeader(\"X-User-Id\");
        if (userId == null || userId.isEmpty()) {
            throw new UnauthorizedException(\"User context not found\");
        }
        return userId;
    }
    
    public static List<String> getUserRoles(HttpServletRequest request) {
        String rolesHeader = request.getHeader(\"X-User-Roles\");
        return Arrays.asList(rolesHeader.split(\",\"));
    }
}
\`\`\`

## Testing

- Grep search for hardcoded values: \`grep -r \\\"\\\"1\\\"\\\" --include=\"*.java\"\`
- Grep search: \`grep -r \"u1001\" --include=\"*.java\"\`
- Unit tests mock request headers
- Integration tests verify user context propagation

## Dependencies

- Can be done in parallel with US-4, US-5, US-6

## Definition of Done

- [ ] No hardcoded user IDs remain in codebase
- [ ] User context utility implemented
- [ ] All tests updated and passing
- [ ] Code review confirms no hardcoded values
- [ ] Documentation updated")

echo "✓ US-7 created: $(echo "$US7_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-8: Database Schema Updates
echo "Creating US-8: Database Schema Updates for RBAC..."
US8_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-8: Database Schema Updates for RBAC" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🔴 HIGH (Foundational)
**Story Points:** 2

## User Story

**As a** database administrator  
**I want to** ensure database schema properly supports RBAC  
**So that** user roles and associations are correctly persisted and performant

## Description

Update database schemas across YSQL tables to support user authentication, role management, and user-specific data isolation. Ensure proper foreign keys, indexes, and constraints are in place.

## Acceptance Criteria

- [ ] YSQL schema includes \`username\`, \`role\`, and \`user_roles\` tables with proper relationships
- [ ] Foreign key constraints enforce referential integrity
- [ ] Indexes created on frequently queried columns:
  - \`username.username\` (unique index for login lookups)
  - \`user_roles.user_id\` (for role lookups)
  - \`shopping_cart.user_id\` (for cart queries)
  - \`orders.user_id\` (for order queries - if column added)
- [ ] \`shopping_cart\` table has \`user_id\` column with proper type and foreign key
- [ ] \`orders\` table updated to store actual \`user_id\` instead of hardcoded value
- [ ] Migration script provided for existing data
- [ ] Schema documentation updated

## Technical Notes

**Schema Updates:**
\`\`\`sql
-- User and Role tables
CREATE TABLE IF NOT EXISTS username (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS role (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES username(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE
);

-- Update shopping_cart if needed
ALTER TABLE shopping_cart 
ADD CONSTRAINT fk_cart_user 
FOREIGN KEY (user_id) REFERENCES username(id);

-- Add indexes
CREATE INDEX idx_username_lookup ON username(username);
CREATE INDEX idx_cart_user ON shopping_cart(user_id);
CREATE INDEX idx_user_roles_user ON user_roles(user_id);
\`\`\`

**YCQL Updates:**
\`\`\`cql
-- Update orders table to properly handle user_id if needed
ALTER TABLE cronos.orders ADD user_id text;
CREATE INDEX ON cronos.orders (user_id);
\`\`\`

## Testing

- Verify foreign key constraints prevent orphaned records
- Test index usage with EXPLAIN queries
- Verify data migration script on test data
- Performance test with indexed queries

## Dependencies

- None (can be done early)

## Definition of Done

- [ ] Schema updates applied to development database
- [ ] Migration scripts tested
- [ ] Indexes created and verified
- [ ] Foreign keys enforced
- [ ] Documentation updated with new schema
- [ ] DBA review completed")

echo "✓ US-8 created: $(echo "$US8_URL" | grep -o '[0-9]*$')"
sleep 1

# Create US-9: Integration Testing
echo "Creating US-9: Integration Testing for RBAC Scenarios..."
US9_URL=$(gh issue create \
  --repo "$REPO" \
  --title "US-9: Integration Testing for RBAC Scenarios" \
  --body "**Part of EPIC #${EPIC_NUMBER}**
**Priority:** 🟡 MEDIUM
**Story Points:** 5

## User Story

**As a** QA engineer  
**I want to** comprehensive integration tests for all RBAC scenarios  
**So that** role-based access control is verified to work correctly across all services

## Description

Create end-to-end integration tests that verify RBAC functionality across the entire application stack, testing all role combinations and authorization scenarios.

## Acceptance Criteria

- [ ] Test suite covers all four roles: ANONYMOUS, CUSTOMER, SUPPORT, ADMIN
- [ ] Positive tests verify authorized access succeeds:
  - Customer can browse products, manage own cart, place orders
  - Support can view all orders and inventory
  - Admin can perform all operations
  - Anonymous can browse products only
- [ ] Negative tests verify unauthorized access is blocked:
  - Customer cannot access another customer's cart/orders
  - Customer cannot modify products or inventory
  - Support cannot modify orders or inventory
  - Anonymous cannot access cart or checkout
- [ ] Cross-service scenarios tested (cart → checkout flow with proper user context)
- [ ] Token expiration scenario tested
- [ ] Invalid token scenario tested
- [ ] Role escalation attempts tested (customer trying admin endpoints)
- [ ] Test report generated with coverage metrics

## Technical Notes

**Test Categories:**
1. **Authentication Tests** - Login, token generation, token validation
2. **Authorization Tests** - Role-based endpoint access
3. **Data Isolation Tests** - User can only access own data
4. **Cross-Service Tests** - User context flows through services
5. **Error Handling Tests** - 401, 403 responses

## Testing

- All integration tests pass
- Test coverage report shows >80% coverage of security code
- Performance test ensures auth checks don't significantly impact response time

## Dependencies

- US-1 through US-8 must be complete
- This is the final validation story

## Definition of Done

- [ ] All test scenarios implemented
- [ ] Tests passing in CI/CD pipeline
- [ ] Test documentation created
- [ ] Coverage report generated
- [ ] No critical security issues found
- [ ] Code reviewed and merged")

echo "✓ US-9 created: $(echo "$US9_URL" | grep -o '[0-9]*$')"
echo ""

echo "=========================================="
echo "✓ All issues created successfully!"
echo "=========================================="
echo ""
echo "Summary:"
echo "  EPIC: #$EPIC_NUMBER"
echo "  US-1: $(echo "$US1_URL" | grep -o '[0-9]*$') - JWT-Based Authentication Infrastructure"
echo "  US-2: $(echo "$US2_URL" | grep -o '[0-9]*$') - Role Management and Assignment"
echo "  US-3: $(echo "$US3_URL" | grep -o '[0-9]*$') - API Gateway Authentication"
echo "  US-4: $(echo "$US4_URL" | grep -o '[0-9]*$') - Secure Products Microservice"
echo "  US-5: $(echo "$US5_URL" | grep -o '[0-9]*$') - Secure Cart Microservice"
echo "  US-6: $(echo "$US6_URL" | grep -o '[0-9]*$') - Secure Checkout Microservice"
echo "  US-7: $(echo "$US7_URL" | grep -o '[0-9]*$') - Remove Hardcoded User IDs"
echo "  US-8: $(echo "$US8_URL" | grep -o '[0-9]*$') - Database Schema Updates"
echo "  US-9: $(echo "$US9_URL" | grep -o '[0-9]*$') - Integration Testing"
echo ""
echo "View all issues at:"
echo "https://github.com/$REPO/issues"
echo ""
