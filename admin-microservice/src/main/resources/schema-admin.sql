-- Admin Portal Schema for YugabyteDB YSQL
-- This script creates tables for admin user management and audit logging

-- Admin Users Table
CREATE TABLE IF NOT EXISTS admin_users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'EDITOR', 'VIEWER')),
    email VARCHAR(255) UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP WITH TIME ZONE,
    is_active BOOLEAN DEFAULT true,
    failed_login_attempts INT DEFAULT 0,
    locked_until TIMESTAMP WITH TIME ZONE
);

-- Product Audit Log Table
CREATE TABLE IF NOT EXISTS product_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    product_asin VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('CREATE', 'UPDATE', 'DELETE', 'DEACTIVATE', 'ACTIVATE', 'BULK_UPDATE')),
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES admin_users(user_id)
);

-- Indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_audit_product ON product_audit_log(product_asin, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_user ON product_audit_log(user_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_timestamp ON product_audit_log(timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_audit_action_type ON product_audit_log(action_type, timestamp DESC);

-- Index for username lookup
CREATE INDEX IF NOT EXISTS idx_admin_username ON admin_users(username) WHERE is_active = true;

-- Comments for documentation
COMMENT ON TABLE admin_users IS 'Stores admin user accounts for the admin portal';
COMMENT ON TABLE product_audit_log IS 'Audit trail for all product changes made through the admin portal';

COMMENT ON COLUMN admin_users.role IS 'User role: ADMIN (full access), EDITOR (create/update), VIEWER (read-only)';
COMMENT ON COLUMN admin_users.failed_login_attempts IS 'Counter for failed login attempts, reset on successful login';
COMMENT ON COLUMN admin_users.locked_until IS 'Account lockout timestamp after too many failed login attempts';

COMMENT ON COLUMN product_audit_log.action_type IS 'Type of action performed: CREATE, UPDATE, DELETE, DEACTIVATE, ACTIVATE, BULK_UPDATE';
COMMENT ON COLUMN product_audit_log.field_name IS 'Name of the field that was modified (null for CREATE/DELETE)';
COMMENT ON COLUMN product_audit_log.old_value IS 'Previous value before change (null for CREATE)';
COMMENT ON COLUMN product_audit_log.new_value IS 'New value after change (null for DELETE)';
COMMENT ON COLUMN product_audit_log.reason IS 'User-provided reason for the change (especially for DELETE operations)';
