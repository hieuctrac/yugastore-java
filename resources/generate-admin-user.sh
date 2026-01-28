#!/bin/bash

# Script to generate a new admin user with BCrypt password hash
# Usage: ./generate-admin-user.sh <username> <password> <role> <email>

set -e

USERNAME=$1
PASSWORD=$2
ROLE=$3
EMAIL=$4

if [ -z "$USERNAME" ] || [ -z "$PASSWORD" ] || [ -z "$ROLE" ] || [ -z "$EMAIL" ]; then
    echo "Usage: $0 <username> <password> <role> <email>"
    echo "Example: $0 john john123 ADMIN john@yugastore.com"
    echo ""
    echo "Valid roles: ADMIN, EDITOR, VIEWER"
    exit 1
fi

# Validate role
if [[ ! "$ROLE" =~ ^(ADMIN|EDITOR|VIEWER)$ ]]; then
    echo "Error: Invalid role. Must be ADMIN, EDITOR, or VIEWER"
    exit 1
fi

echo "Generating BCrypt hash for password..."

# Generate BCrypt hash using Java (requires compiled admin-microservice)
HASH=$(cd ../admin-microservice && mvn -q exec:java -Dexec.mainClass="org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder" -Dexec.args="$PASSWORD" 2>/dev/null || echo "")

if [ -z "$HASH" ]; then
    echo "Warning: Could not generate BCrypt hash using Maven."
    echo "Please use an online BCrypt generator (rounds=10) or Java:"
    echo ""
    echo "  String hash = new BCryptPasswordEncoder().encode(\"$PASSWORD\");"
    echo ""
    echo "SQL to insert manually:"
else
    echo "BCrypt hash generated successfully!"
    echo ""
fi

# Generate SQL INSERT statement
cat <<EOF

-- Generated Admin User SQL
-- Username: $USERNAME
-- Password: $PASSWORD (plaintext - for reference only, not stored)
-- Role: $ROLE
-- Email: $EMAIL

INSERT INTO admin_users (username, password_hash, role, email, is_active, created_at)
VALUES (
    '$USERNAME',
    '$HASH',
    '$ROLE',
    '$EMAIL',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Verify user was created
SELECT username, role, email, is_active, created_at
FROM admin_users
WHERE username = '$USERNAME';

EOF

echo "To execute this SQL, run:"
echo "  psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte -f <filename>.sql"
echo ""
echo "Or pipe directly:"
echo "  psql -h 127.0.0.1 -p 5433 -U yugabyte -d yugabyte <<< \"<paste SQL above>\""
