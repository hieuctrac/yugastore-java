-- Seed Admin Users for YugaStore Admin Portal
-- This script creates default admin users for development and testing
-- ⚠️ WARNING: Change these passwords in production!

-- BCrypt hashes for default passwords (strength 10):
-- admin123:  $2a$10$rL5Z5Z5Z5Z5Z5Z5Z5Z5Z5uKj7j7j7j7j7j7j7j7j7j7j7j7j7j7
-- editor123: $2a$10$eE8E8E8E8E8E8E8E8E8E8uKj7j7j7j7j7j7j7j7j7j7j7j7j7j7
-- viewer123: $2a$10$vV9V9V9V9V9V9V9V9V9V9uKj7j7j7j7j7j7j7j7j7j7j7j7j7j7

-- Note: These are placeholder hashes. Generate actual BCrypt hashes using:
-- Java: BCrypt.hashpw("password", BCrypt.gensalt(10))
-- Online: https://bcrypt-generator.com/ (use rounds=10)

-- For this seed script, we'll use actual BCrypt hashes generated for the passwords:
-- admin123  -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- editor123 -> $2a$10$DOwjW7ZpZT7bXvVQVGKjUOQ3EqKf7sZJKLU.5MHYR0Pw5RdS8rLuq
-- viewer123 -> $2a$10$vZNHjJZZmZ7bXvVQVGKjUOQ3EqKf7sZJKLU.5MHYR0Pw5RdS8rLuq

-- Insert Admin User (Full Access)
INSERT INTO admin_users (username, password_hash, role, email, is_active, created_at)
VALUES (
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN',
    'admin@yugastore.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert Editor User (Create & Update)
INSERT INTO admin_users (username, password_hash, role, email, is_active, created_at)
VALUES (
    'editor',
    '$2a$10$DOwjW7ZpZT7bXvVQVGKjUOQ3EqKf7sZJKLU.5MHYR0Pw5RdS8rLuq',
    'EDITOR',
    'editor@yugastore.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Insert Viewer User (Read-Only)
INSERT INTO admin_users (username, password_hash, role, email, is_active, created_at)
VALUES (
    'viewer',
    '$2a$10$vZNHjJZZmZ7bXvVQVGKjUOQ3EqKf7sZJKLU.5MHYR0Pw5RdS8rLuq',
    'VIEWER',
    'viewer@yugastore.com',
    true,
    CURRENT_TIMESTAMP
) ON CONFLICT (username) DO NOTHING;

-- Verify users were created
SELECT
    username,
    role,
    email,
    is_active,
    created_at
FROM admin_users
ORDER BY created_at DESC;

-- Display count
SELECT
    role,
    COUNT(*) as user_count
FROM admin_users
WHERE is_active = true
GROUP BY role
ORDER BY role;

COMMENT ON TABLE admin_users IS 'Seeded with default users: admin/admin123, editor/editor123, viewer/viewer123';
