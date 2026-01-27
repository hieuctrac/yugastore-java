#!/bin/bash

# =============================================
# RBAC Authentication Schema Execution Script
# =============================================
# This script executes the auth schema creation in YugabyteDB YSQL
# Run this script when YugabyteDB is available

set -e  # Exit on any error

echo "🚀 Executing RBAC Authentication Schema..."
echo "=========================================="

# Configuration
YSQL_HOST=${YSQL_HOST:-localhost}
YSQL_PORT=${YSQL_PORT:-5433}
YSQL_USER=${YSQL_USER:-yugabyte}
YSQL_DATABASE=${YSQL_DATABASE:-yugabyte}

# Check if ysqlsh is available
if ! command -v ysqlsh &> /dev/null; then
    echo "❌ Error: ysqlsh command not found"
    echo "   Please install YugabyteDB or ensure ysqlsh is in your PATH"
    echo "   Installation: https://docs.yugabyte.com/latest/quick-start/"
    exit 1
fi

# Check if YugabyteDB is running
echo "🔍 Checking YugabyteDB connectivity..."
if ! ysqlsh -h "$YSQL_HOST" -p "$YSQL_PORT" -U "$YSQL_USER" -d "$YSQL_DATABASE" -c "SELECT 1;" &> /dev/null; then
    echo "❌ Error: Cannot connect to YugabyteDB"
    echo "   Host: $YSQL_HOST:$YSQL_PORT"
    echo "   User: $YSQL_USER"
    echo "   Database: $YSQL_DATABASE"
    echo ""
    echo "   Please ensure YugabyteDB is running:"
    echo "   yugabyted start"
    exit 1
fi

echo "✅ YugabyteDB connection successful"

# Execute the auth schema
echo "📝 Creating RBAC authentication schema..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_FILE="$SCRIPT_DIR/001-create-auth-tables.sql"

if [[ ! -f "$SCHEMA_FILE" ]]; then
    echo "❌ Error: Schema file not found: $SCHEMA_FILE"
    exit 1
fi

echo "   Executing: $SCHEMA_FILE"
if ysqlsh -h "$YSQL_HOST" -p "$YSQL_PORT" -U "$YSQL_USER" -d "$YSQL_DATABASE" -f "$SCHEMA_FILE"; then
    echo "✅ RBAC authentication schema created successfully!"

    # Verify tables were created
    echo ""
    echo "🔍 Verifying schema creation..."
    echo "   Tables created:"
    ysqlsh -h "$YSQL_HOST" -p "$YSQL_PORT" -U "$YSQL_USER" -d "$YSQL_DATABASE" -c "
        SELECT tablename
        FROM pg_tables
        WHERE tablename IN ('users', 'roles', 'user_roles', 'user_sessions', 'password_reset_tokens', 'audit_logs')
        ORDER BY tablename;
    "

    echo ""
    echo "   Default roles:"
    ysqlsh -h "$YSQL_HOST" -p "$YSQL_PORT" -U "$YSQL_USER" -d "$YSQL_DATABASE" -c "
        SELECT role_name, description
        FROM roles
        ORDER BY role_name;
    "

    echo ""
    echo "🎉 RBAC system database schema is ready!"
    echo "   Next steps:"
    echo "   1. Configure JWT secrets in microservices"
    echo "   2. Implement authentication services"
    echo "   3. Test user registration and login flows"

else
    echo "❌ Error: Failed to create authentication schema"
    echo "   Check the error messages above for details"
    exit 1
fi