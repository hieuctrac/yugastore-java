-- =============================================
-- Migration Script Template
-- =============================================
-- Version: XXX-description
-- Date: YYYY-MM-DD
-- Author: [Your Name]
-- Description: [Brief description of changes]
--
-- Dependencies: [List any required prior migrations]
-- Rollback: [Reference to rollback script if available]
-- =============================================

-- Start transaction for atomic changes
BEGIN;

-- =============================================
-- Pre-migration Checks
-- =============================================

-- Verify prerequisites (uncomment and modify as needed)
-- DO $$
-- BEGIN
--     IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'users') THEN
--         RAISE EXCEPTION 'Required table "users" does not exist. Run prerequisite migrations first.';
--     END IF;
-- END $$;

-- =============================================
-- Schema Changes
-- =============================================

-- Example: Create new table
-- CREATE TABLE IF NOT EXISTS example_table (
--     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
--     name VARCHAR(255) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );

-- Example: Add new column
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS new_field VARCHAR(100);

-- Example: Create index
-- CREATE INDEX IF NOT EXISTS idx_example_name ON example_table(name);

-- =============================================
-- Data Migration
-- =============================================

-- Example: Insert default data
-- INSERT INTO example_table (name) VALUES ('default_value')
-- ON CONFLICT (name) DO NOTHING;

-- Example: Update existing data
-- UPDATE users SET new_field = 'default' WHERE new_field IS NULL;

-- =============================================
-- Post-migration Validation
-- =============================================

-- Verify migration success (uncomment and modify as needed)
-- DO $$
-- BEGIN
--     IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'users' AND column_name = 'new_field') THEN
--         RAISE EXCEPTION 'Migration failed: new_field column was not created';
--     END IF;
-- END $$;

-- Commit transaction
COMMIT;

-- =============================================
-- Migration Notes
-- =============================================
-- [Add any additional notes about this migration]
-- [Include performance considerations]
-- [Document any manual steps required]