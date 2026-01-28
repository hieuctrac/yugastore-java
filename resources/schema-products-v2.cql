-- Schema Extension for Admin Portal Product Management
-- This script extends the existing products table with admin-specific fields
-- These fields support soft deletion and optimistic locking

-- Note: CQL does not support ALTER TABLE ADD COLUMN IF NOT EXISTS in older versions
-- This script assumes the fields do not already exist
-- If running on an existing database, check if fields exist first

-- Add admin portal fields to products table
-- These would be added via ALTER TABLE commands:
-- ALTER TABLE cronos.products ADD is_active boolean;
-- ALTER TABLE cronos.products ADD deactivated_at timestamp;
-- ALTER TABLE cronos.products ADD deactivated_by text;
-- ALTER TABLE cronos.products ADD version int;

-- For new installations, create the products table with admin fields included:

CREATE TABLE IF NOT EXISTS cronos.products_admin_v2 (
    asin text PRIMARY KEY,
    title text,
    description text,
    price double,
    imurl text,
    also_bought frozen<list<text>>,
    also_viewed frozen<list<text>>,
    bought_together frozen<list<text>>,
    buy_after_viewing frozen<list<text>>,
    brand text,
    categories set<text>,
    num_reviews int,
    num_stars int,
    avg_stars double,
    -- Admin portal fields
    is_active boolean,
    deactivated_at timestamp,
    deactivated_by text,
    version int
) WITH default_time_to_live = 0
    AND transactions = {'enabled': 'false'};

-- Migration note:
-- If upgrading from existing products table, you would:
-- 1. Use ALTER TABLE to add the new columns to cronos.products
-- 2. Run UPDATE statements to initialize is_active=true and version=0 for existing products
--
-- Example migration commands (run these via cqlsh if needed):
-- ALTER TABLE cronos.products ADD is_active boolean;
-- ALTER TABLE cronos.products ADD deactivated_at timestamp;
-- ALTER TABLE cronos.products ADD deactivated_by text;
-- ALTER TABLE cronos.products ADD version int;
--
-- UPDATE cronos.products SET is_active=true, version=0 WHERE is_active=null;
