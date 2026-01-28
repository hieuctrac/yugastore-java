#!/bin/bash

# Test script for Admin Portal
# This script verifies the admin microservice is working correctly

set -e

BASE_URL="http://localhost:8084"
ADMIN_API="$BASE_URL/api/admin"

echo "=================================="
echo "Admin Portal Test Script"
echo "=================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to test endpoint
test_endpoint() {
    local name=$1
    local url=$2
    local method=$3
    local data=$4
    local expected_status=$5
    local auth_header=$6

    echo -n "Testing: $name... "

    if [ -n "$auth_header" ]; then
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -H "Authorization: Bearer $auth_header" \
                -d "$data" 2>/dev/null || echo "000")
        else
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Authorization: Bearer $auth_header" 2>/dev/null || echo "000")
        fi
    else
        if [ -n "$data" ]; then
            response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
                -H "Content-Type: application/json" \
                -d "$data" 2>/dev/null || echo "000")
        else
            response=$(curl -s -w "\n%{http_code}" "$url" 2>/dev/null || echo "000")
        fi
    fi

    status_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)

    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (Status: $status_code)"
        TESTS_PASSED=$((TESTS_PASSED + 1))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected: $expected_status, Got: $status_code)"
        echo "Response: $body"
        TESTS_FAILED=$((TESTS_FAILED + 1))
        return 1
    fi
}

echo "Step 1: Check if service is running..."
if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Service not running on port 8084${NC}"
    echo "Please start the admin microservice first:"
    echo "  cd admin-microservice && mvn spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✓ Service is running${NC}"
echo ""

echo "Step 2: Test health endpoint..."
test_endpoint "Health Check" "$BASE_URL/actuator/health" "GET" "" "200"
echo ""

echo "Step 3: Test authentication..."

# Test login with admin user
echo "Logging in as admin..."
login_response=$(curl -s -X POST "$ADMIN_API/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin123"}')

if echo "$login_response" | grep -q '"token"'; then
    echo -e "${GREEN}✓ Login successful${NC}"
    TOKEN=$(echo "$login_response" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    echo "JWT Token: ${TOKEN:0:50}..."
    TESTS_PASSED=$((TESTS_PASSED + 1))
else
    echo -e "${RED}✗ Login failed${NC}"
    echo "Response: $login_response"
    echo ""
    echo -e "${YELLOW}Note: Make sure you have run the seed script:${NC}"
    echo "  psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f ../resources/seed-admin-users.sql"
    TESTS_FAILED=$((TESTS_FAILED + 1))
    TOKEN=""
fi
echo ""

if [ -n "$TOKEN" ]; then
    echo "Step 4: Test authenticated endpoints..."
    test_endpoint "Get Current User" "$ADMIN_API/auth/me" "GET" "" "200" "$TOKEN"
    echo ""

    echo "Step 5: Test logout..."
    test_endpoint "Logout" "$ADMIN_API/auth/logout" "POST" "" "200" "$TOKEN"
    echo ""
fi

echo "Step 6: Test invalid credentials..."
test_endpoint "Invalid Login" "$ADMIN_API/auth/login" "POST" '{"username":"invalid","password":"wrong"}' "401"
echo ""

echo "Step 7: Test unauthorized access..."
test_endpoint "Unauthorized /me" "$ADMIN_API/auth/me" "GET" "" "401"
echo ""

echo "=================================="
echo "Test Summary"
echo "=================================="
echo -e "Total Tests: $((TESTS_PASSED + TESTS_FAILED))"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}✓ All tests passed!${NC}"
    echo ""
    echo "Admin Portal is working correctly."
    echo "You can now access the React UI at: http://localhost:3000/admin/login"
    exit 0
else
    echo -e "${YELLOW}⚠ Some tests failed${NC}"
    echo ""
    echo "Troubleshooting steps:"
    echo "1. Ensure YugabyteDB is running: yugabyted status"
    echo "2. Initialize database schema: psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f src/main/resources/schema-admin.sql"
    echo "3. Create admin users: psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f ../resources/seed-admin-users.sql"
    echo "4. Check service logs for errors"
    exit 1
fi
