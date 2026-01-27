-- =============================================
-- Migration Script: Initial RBAC Authentication Schema
-- =============================================
-- Version: 001-create-auth-tables
-- Date: 2026-01-27
-- Author: Claude Code
-- Description: Creates the foundational RBAC authentication schema with users, roles, sessions, and audit tables
--
-- Dependencies: YugabyteDB YSQL instance must be running
-- Rollback: Available as rollback-001-create-auth-tables.sql
-- =============================================

-- This script creates the complete authentication schema for the YugaStore RBAC system
-- Execute using: ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -f 001-create-auth-tables.sql

-- Start transaction for atomic changes
BEGIN;

-- =============================================
-- User Management Tables
-- =============================================

-- Users table for authentication and profile data
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,  -- bcrypt hashed password
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    is_email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- =============================================
-- Role Management Tables
-- =============================================

-- Roles table for RBAC system
CREATE TABLE IF NOT EXISTS roles (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_name VARCHAR(50) UNIQUE NOT NULL,  -- ROLE_ANONYMOUS, ROLE_CUSTOMER, ROLE_SUPPORT, ROLE_ADMIN
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default roles
INSERT INTO roles (role_name, description) VALUES
('ROLE_ANONYMOUS', 'Unauthenticated users - can browse products only')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, description) VALUES
('ROLE_CUSTOMER', 'Registered customers - can manage own cart, orders, and profile')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, description) VALUES
('ROLE_SUPPORT', 'Customer support representatives - read-only access to customer data')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name, description) VALUES
('ROLE_ADMIN', 'System administrators - full access with audit logging')
ON CONFLICT (role_name) DO NOTHING;

-- User-Role junction table (many-to-many relationship)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    role_id UUID REFERENCES roles(role_id) ON DELETE CASCADE,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by UUID REFERENCES users(user_id),  -- Who assigned this role
    is_active BOOLEAN DEFAULT true,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- =============================================
-- Session Management Tables
-- =============================================

-- User sessions for tracking active logins
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    jwt_token_id VARCHAR(255) UNIQUE NOT NULL,  -- JWT jti claim for token identification
    device_info TEXT,  -- User agent, IP address, etc.
    ip_address INET,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN DEFAULT true
);

CREATE INDEX IF NOT EXISTS idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX IF NOT EXISTS idx_sessions_jwt_token_id ON user_sessions(jwt_token_id);
CREATE INDEX IF NOT EXISTS idx_sessions_expires_at ON user_sessions(expires_at);
CREATE INDEX IF NOT EXISTS idx_sessions_active ON user_sessions(is_active);

-- =============================================
-- Password Reset Tables
-- =============================================

-- Password reset tokens for secure password recovery
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id) ON DELETE CASCADE,
    reset_token VARCHAR(255) UNIQUE NOT NULL,  -- Secure random token
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT false,
    used_at TIMESTAMP,
    ip_address INET  -- Track IP for security
);

CREATE INDEX IF NOT EXISTS idx_reset_tokens_user_id ON password_reset_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_token ON password_reset_tokens(reset_token);
CREATE INDEX IF NOT EXISTS idx_reset_tokens_expires_at ON password_reset_tokens(expires_at);

-- =============================================
-- Audit Logging Tables
-- =============================================

-- Audit log for tracking all authentication and authorization events
CREATE TABLE IF NOT EXISTS audit_logs (
    audit_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(user_id),  -- NULL for anonymous actions
    action VARCHAR(100) NOT NULL,  -- LOGIN, LOGOUT, REGISTER, PASSWORD_RESET, ROLE_ASSIGN, etc.
    resource_type VARCHAR(50),  -- USER, ROLE, PRODUCT, CART, ORDER, etc.
    resource_id VARCHAR(255),   -- ID of the affected resource
    details JSONB,  -- Additional context (old/new values, etc.)
    ip_address INET,
    user_agent TEXT,
    success BOOLEAN NOT NULL,
    error_message TEXT,  -- If success=false
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_action ON audit_logs(action);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs(created_at);
CREATE INDEX IF NOT EXISTS idx_audit_logs_success ON audit_logs(success);

-- =============================================
-- Trigger Functions for Updated_At
-- =============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for users table
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- =============================================
-- Views for Common Queries
-- =============================================

-- View for user details with roles
CREATE OR REPLACE VIEW user_details AS
SELECT
    u.user_id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.is_active,
    u.is_email_verified,
    u.created_at,
    u.last_login_at,
    ARRAY_AGG(r.role_name) as roles
FROM users u
LEFT JOIN user_roles ur ON u.user_id = ur.user_id AND ur.is_active = true
LEFT JOIN roles r ON ur.role_id = r.role_id AND r.is_active = true
WHERE u.is_active = true
GROUP BY u.user_id, u.username, u.email, u.first_name, u.last_name, u.is_active, u.is_email_verified, u.created_at, u.last_login_at;

-- =============================================
-- Post-migration Validation
-- =============================================

-- Verify all required tables were created
DO $$
BEGIN
    -- Check users table
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
        RAISE EXCEPTION 'Migration failed: users table was not created';
    END IF;

    -- Check roles table and default roles
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'roles') THEN
        RAISE EXCEPTION 'Migration failed: roles table was not created';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM roles WHERE role_name = 'ROLE_CUSTOMER') THEN
        RAISE EXCEPTION 'Migration failed: default roles were not inserted';
    END IF;

    -- Check other critical tables
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'user_sessions') THEN
        RAISE EXCEPTION 'Migration failed: user_sessions table was not created';
    END IF;

    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'audit_logs') THEN
        RAISE EXCEPTION 'Migration failed: audit_logs table was not created';
    END IF;

    RAISE NOTICE 'Migration completed successfully: RBAC authentication schema created';
END $$;

-- Commit transaction
COMMIT;

-- =============================================
-- Table Comments and Documentation
-- =============================================

COMMENT ON TABLE users IS 'Core user authentication and profile data';
COMMENT ON TABLE roles IS 'System roles for RBAC (ANONYMOUS, CUSTOMER, SUPPORT, ADMIN)';
COMMENT ON TABLE user_roles IS 'Junction table linking users to their assigned roles';
COMMENT ON TABLE user_sessions IS 'Active user sessions with JWT token tracking';
COMMENT ON TABLE password_reset_tokens IS 'Secure password reset tokens with expiration';
COMMENT ON TABLE audit_logs IS 'Comprehensive audit trail for all authentication/authorization events';

COMMENT ON COLUMN users.password_hash IS 'bcrypt hashed password with salt';
COMMENT ON COLUMN user_sessions.jwt_token_id IS 'JWT jti claim for token revocation';
COMMENT ON COLUMN audit_logs.details IS 'JSONB field for flexible audit context';