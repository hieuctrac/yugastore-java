# Data Model: Admin Portal for Product Management

**Feature**: 002-admin-portal
**Date**: 2026-01-27
**Status**: Complete

## Overview

This document defines the data entities, their relationships, validation rules, and state transitions for the Admin Portal feature. Entities are derived from the feature specification and aligned with research decisions.

---

## Entity Relationship Diagram

```
┌─────────────────┐          ┌──────────────────────┐
│   AdminUser     │          │   ProductMetadata    │
│                 │          │   (existing YCQL)    │
│  - user_id (PK) │          │   - asin (PK)        │
│  - username     │          │   - title            │
│  - role         │          │   - category         │
└────────┬────────┘          │   - price            │
         │                   │   - is_active (NEW)  │
         │                   └──────────┬───────────┘
         │ creates                      │
         │                              │
         │  ┌───────────────────────────┘
         │  │ audits
         │  │
         ▼  ▼
┌──────────────────────────┐
│   ProductAuditLog        │
│                          │
│  - audit_id (PK)         │
│  - product_asin (FK)     │
│  - user_id (FK)          │
│  - action_type           │
│  - timestamp             │
└──────────────────────────┘
```

---

## Entity 1: AdminUser

### Description
Represents an authenticated user of the admin portal with a specific role determining their access permissions.

### Storage
- **Database**: YugabyteDB YSQL
- **Table**: `admin_users`
- **Schema**: See SQL definition below

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| user_id | UUID | PRIMARY KEY, NOT NULL | Unique identifier for admin user |
| username | VARCHAR(100) | UNIQUE, NOT NULL | Login username |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt hashed password |
| role | VARCHAR(20) | NOT NULL, CHECK IN ('ADMIN', 'EDITOR', 'VIEWER') | User role for RBAC |
| email | VARCHAR(255) | NULLABLE | Admin user email (for password reset) |
| created_at | TIMESTAMP | DEFAULT NOW() | Account creation timestamp |
| last_login | TIMESTAMP | NULLABLE | Last successful login timestamp |

### Validation Rules

**Username** (FR-001, FR-002):
- Required
- 3-100 characters
- Must be unique
- Alphanumeric and underscore only (regex: `^[a-zA-Z0-9_]{3,100}$`)

**Password** (FR-001):
- Minimum 8 characters
- Must contain: 1 uppercase, 1 lowercase, 1 digit, 1 special character
- Stored as BCrypt hash (never plaintext)

**Role** (FR-002):
- Must be one of: ADMIN, EDITOR, VIEWER
- Cannot be null
- Determines authorization permissions

**Email** (FR-005):
- Optional
- Valid email format if provided
- Used for password reset functionality

### Role Permissions

| Role | Create Product | Update Product | Deactivate | Delete | View | Bulk Operations |
|------|----------------|----------------|------------|--------|------|-----------------|
| ADMIN | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ |
| EDITOR | ✓ | ✓ | ✓ | ✗ | ✓ | ✓ (except delete) |
| VIEWER | ✗ | ✗ | ✗ | ✗ | ✓ | ✗ |

### State Transitions

```
[No Account] --register--> [Active]
[Active] --login--> [Authenticated] (JWT issued)
[Authenticated] --30min idle--> [Session Expired] (FR-004)
[Authenticated] --logout--> [Active]
```

### SQL Schema

```sql
CREATE TABLE admin_users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'EDITOR', 'VIEWER')),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT NOW(),
    last_login TIMESTAMP
);

CREATE INDEX idx_admin_username ON admin_users(username);
```

### Java Domain Model

```java
@Entity
@Table(name = "admin_users")
public class AdminUser {
    @Id
    @GeneratedValue
    private UUID userId;

    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @Column(nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    @Column(length = 255)
    private String email;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastLogin;

    // Getters, setters, constructors
}

public enum AdminRole {
    ADMIN,
    EDITOR,
    VIEWER
}
```

---

## Entity 2: ProductMetadata (Extended)

### Description
Represents a product in the YugaStore catalog. This is an **existing entity** that will be extended with admin-specific fields for soft delete functionality.

### Storage
- **Database**: YugabyteDB YCQL (Cassandra-compatible)
- **Table**: `products` (existing)
- **Schema Extension**: See CQL definition below

### Existing Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| asin | TEXT | PRIMARY KEY | Amazon Standard Identification Number |
| title | TEXT | NOT NULL | Product title |
| category | TEXT | NOT NULL | Product category |
| author | TEXT | NOT NULL | Product author/creator |
| price | DECIMAL | NOT NULL | Product price |
| description | TEXT | NULLABLE | Short product description |
| long_description | TEXT | NULLABLE | Detailed product description |
| image_url | TEXT | NULLABLE | Product image URL |
| quantity | INT | DEFAULT 0 | Inventory quantity |

### NEW Fields (Admin Portal Extension)

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| is_active | BOOLEAN | DEFAULT TRUE | Soft delete flag (FR-032 to FR-036) |
| deactivated_at | TIMESTAMP | NULLABLE | When product was deactivated |
| deactivated_by | TEXT | NULLABLE | Admin user ID who deactivated |
| version | INT | DEFAULT 0 | Optimistic locking version (FR-021) |

### Validation Rules

**ASIN** (FR-023):
- Required for creation
- 1-50 characters
- Must be unique across all products
- Immutable after creation (read-only in updates)

**Title** (FR-024):
- Required
- 1-500 characters
- Cannot be empty string

**Category** (FR-025):
- Required
- Must exist in valid categories list
- Referenced from category enum/table

**Author** (FR-026):
- Required
- 1-200 characters

**Price** (FR-027):
- Required
- Positive decimal (> 0)
- Maximum 2 decimal places
- Maximum value: $99,999.99

**Quantity** (FR-028):
- Optional, defaults to 0
- Non-negative integer (>= 0)
- Maximum value: 999,999
- Quantity = 0 means out of stock (FR-022)

**Image URL** (FR-029):
- Optional
- Must be valid HTTP/HTTPS URL if provided
- Regex: `^https?://.*`

**Description** (FR-030):
- Optional
- Maximum 1,000 characters

**Long Description** (FR-031):
- Optional
- Maximum 5,000 characters

**is_active** (FR-032 to FR-036):
- Defaults to TRUE for new products (FR-015)
- FALSE = product is deactivated (soft deleted)
- Controls visibility in customer-facing catalog

### State Transitions

```
[New] --create--> [Active] (is_active = TRUE)

[Active] --deactivate--> [Inactive] (is_active = FALSE)
                         (deactivated_at = NOW, deactivated_by = user_id)

[Inactive] --reactivate--> [Active] (is_active = TRUE)
                           (deactivated_at = NULL, deactivated_by = NULL)

[Inactive] --delete (hard)--> [Deleted] (record removed)
                               (only if not in active carts/pending orders)
```

### CQL Schema Extension

```cql
-- Add new columns to existing products table
ALTER TABLE products ADD is_active BOOLEAN;
ALTER TABLE products ADD deactivated_at TIMESTAMP;
ALTER TABLE products ADD deactivated_by TEXT;
ALTER TABLE products ADD version INT;

-- Create index for filtering active/inactive products
CREATE INDEX ON products (is_active);

-- Update existing products to active by default
UPDATE products SET is_active = TRUE WHERE is_active = NULL;
UPDATE products SET version = 0 WHERE version = NULL;
```

### Java Domain Model (Extended)

```java
@Table(name = "products")
public class ProductMetadata {
    @PrimaryKey
    private String asin;

    @Column
    private String title;

    @Column
    private String category;

    @Column
    private String author;

    @Column
    private BigDecimal price;

    @Column
    private String description;

    @Column("long_description")
    private String longDescription;

    @Column("image_url")
    private String imageUrl;

    @Column
    private Integer quantity;

    // NEW: Admin fields
    @Column("is_active")
    private Boolean isActive = true;

    @Column("deactivated_at")
    private LocalDateTime deactivatedAt;

    @Column("deactivated_by")
    private String deactivatedBy;

    @Column
    private Integer version = 0;

    // Getters, setters, constructors
}
```

---

## Entity 3: ProductAuditLog

### Description
Records all changes made to products through the admin portal for compliance, troubleshooting, and accountability.

### Storage
- **Database**: YugabyteDB YSQL
- **Table**: `product_audit_log`
- **Retention**: Minimum 90 days (FR-055)

### Fields

| Field | Type | Constraints | Description |
|-------|------|-------------|-------------|
| audit_id | BIGSERIAL | PRIMARY KEY | Unique audit log entry ID |
| product_asin | VARCHAR(50) | NOT NULL, INDEXED | Product identifier |
| user_id | UUID | NOT NULL, FK to admin_users | Admin user who performed action |
| action_type | VARCHAR(50) | NOT NULL | Type of action (CREATE, UPDATE, DELETE, etc.) |
| field_name | VARCHAR(100) | NULLABLE | Field that changed (for UPDATE) |
| old_value | TEXT | NULLABLE | Previous value (for UPDATE) |
| new_value | TEXT | NULLABLE | New value (for UPDATE/CREATE) |
| reason | TEXT | NULLABLE | Reason for change (required for DELETE) |
| ip_address | INET | NULLABLE | Client IP address |
| timestamp | TIMESTAMP | DEFAULT NOW(), INDEXED | When action occurred |

### Action Types

| Action Type | Description | Required Fields |
|-------------|-------------|-----------------|
| CREATE | Product created | new_value (full product JSON) |
| UPDATE | Product field updated | field_name, old_value, new_value |
| DEACTIVATE | Product deactivated | - |
| ACTIVATE | Product reactivated | - |
| DELETE | Product permanently deleted | reason (mandatory) |
| BULK_UPDATE | Bulk operation performed | new_value (operation description) |

### Validation Rules

**product_asin** (FR-053):
- Required
- Must reference a valid (or formerly valid) product
- Retained even after product is deleted (for audit history)

**user_id** (FR-053):
- Required
- Foreign key to admin_users.user_id
- Records who performed the action

**action_type** (FR-053):
- Required
- Must be one of the defined action types
- Used for filtering and reporting

**timestamp** (FR-053, FR-054):
- Auto-generated on insert
- Immutable
- Used for chronological ordering and retention policy

**reason** (FR-040):
- Required for DELETE action type
- Optional for other actions
- Free text or selected from predefined list

### Indexes

```sql
-- Performance indexes for common queries
CREATE INDEX idx_audit_product ON product_audit_log(product_asin, timestamp DESC);
CREATE INDEX idx_audit_user ON product_audit_log(user_id, timestamp DESC);
CREATE INDEX idx_audit_timestamp ON product_audit_log(timestamp DESC);
CREATE INDEX idx_audit_action ON product_audit_log(action_type, timestamp DESC);
```

### Retention Policy

Per FR-055, audit logs must be retained for minimum 90 days. Implementation options:

**Option 1: Partition by time**
```sql
-- Create monthly partitions
CREATE TABLE product_audit_log_2026_01 PARTITION OF product_audit_log
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
```

**Option 2: Background job for cleanup**
```sql
-- Delete logs older than 90 days (run daily)
DELETE FROM product_audit_log
WHERE timestamp < NOW() - INTERVAL '90 days';
```

### SQL Schema

```sql
CREATE TABLE product_audit_log (
    audit_id BIGSERIAL PRIMARY KEY,
    product_asin VARCHAR(50) NOT NULL,
    user_id UUID NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    reason TEXT,
    ip_address INET,
    timestamp TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES admin_users(user_id)
);

CREATE INDEX idx_audit_product ON product_audit_log(product_asin, timestamp DESC);
CREATE INDEX idx_audit_user ON product_audit_log(user_id, timestamp DESC);
CREATE INDEX idx_audit_timestamp ON product_audit_log(timestamp DESC);
CREATE INDEX idx_audit_action ON product_audit_log(action_type, timestamp DESC);
```

### Java Domain Model

```java
@Entity
@Table(name = "product_audit_log")
public class ProductAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;

    @Column(name = "product_asin", nullable = false, length = 50)
    private String productAsin;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private AdminUser adminUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // Getters, setters, constructors
}

public enum AuditActionType {
    CREATE,
    UPDATE,
    DEACTIVATE,
    ACTIVATE,
    DELETE,
    BULK_UPDATE
}
```

---

## Entity 4: Category (Reference)

### Description
Represents a product category used for classification. This is an **existing entity** that admin portal references but does not manage (category CRUD is out of scope per spec).

### Storage
- **Database**: Implementation TBD (could be YCQL table, enum, or configuration)
- **Usage**: Referenced in ProductMetadata.category field

### Fields (Conceptual)

| Field | Type | Description |
|-------|------|-------------|
| category_id | TEXT/UUID | Unique category identifier |
| category_name | TEXT | Display name |
| description | TEXT | Category description |

### Validation

Products must reference a valid category (FR-025). Categories are assumed to be pre-existing and managed outside this feature scope.

### Example Categories

Based on YugaStore context (book store):
- Fiction
- Non-Fiction
- Science Fiction
- Biography
- History
- Technology
- Children's Books

---

## Relationships Summary

### AdminUser ← ProductAuditLog (1:N)
- One admin user creates many audit log entries
- Foreign key: `product_audit_log.user_id` → `admin_users.user_id`
- Cascade: No delete cascade (retain audit history even if user deleted)

### Product ← ProductAuditLog (1:N)
- One product has many audit log entries
- No formal FK (product in YCQL, audit in YSQL)
- Linked by: `product_audit_log.product_asin` → `products.asin`
- Audit logs retained even after product deletion

### Product → Category (N:1)
- Many products belong to one category
- No formal FK (category management out of scope)
- Linked by: `products.category` → `categories.category_name`

---

## Data Integrity Rules

### Referential Integrity

1. **Audit logs must reference valid admin user** (enforced by FK)
2. **Product category must exist** (enforced by application validation)
3. **ASIN uniqueness** (enforced by YCQL primary key)
4. **Username uniqueness** (enforced by YSQL unique constraint)

### Business Rules

1. **Cannot delete active product** (FR-038): Product must be deactivated first
2. **Cannot delete product in active cart** (FR-041): Blocked by application check
3. **Cannot delete product in pending order** (FR-042): Blocked by application check
4. **Delete requires reason** (FR-040): Application validation
5. **Password complexity** (Security best practice): Validated on create/update
6. **Concurrent update detection** (FR-021): Optimistic locking via version field

---

## Migration Strategy

### Phase 1: Add admin_users and product_audit_log tables
```sql
-- Run on YSQL
CREATE TABLE admin_users (...);
CREATE TABLE product_audit_log (...);
```

### Phase 2: Extend products table
```cql
-- Run on YCQL
ALTER TABLE products ADD is_active BOOLEAN;
ALTER TABLE products ADD deactivated_at TIMESTAMP;
ALTER TABLE products ADD deactivated_by TEXT;
ALTER TABLE products ADD version INT;
CREATE INDEX ON products (is_active);
```

### Phase 3: Backfill existing products
```cql
-- Mark all existing products as active
UPDATE products SET is_active = TRUE WHERE is_active = NULL;
UPDATE products SET version = 0 WHERE version = NULL;
```

### Phase 4: Create initial admin user
```sql
-- Create default admin user (password should be changed on first login)
INSERT INTO admin_users (username, password_hash, role, email)
VALUES ('admin', '$2a$10$...', 'ADMIN', 'admin@yugastore.com');
```

---

## Summary

This data model supports all functional requirements from the spec:
- **AdminUser**: Role-based access control (FR-002, FR-003)
- **ProductMetadata (extended)**: Soft delete with is_active field (FR-032 to FR-036), optimistic locking with version (FR-021)
- **ProductAuditLog**: Comprehensive audit trail (FR-053 to FR-057)
- **Category**: Reference for product classification (FR-025)

All validation rules are derived from FR-023 through FR-031. State transitions support the user stories defined in the specification. Database schemas are optimized for query patterns (indexes on commonly filtered fields).
