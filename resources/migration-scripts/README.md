# Database Migration Scripts

This directory contains SQL migration scripts for the YugaStore RBAC system.

## Directory Structure

```
migration-scripts/
├── README.md              # This file
├── 001-initial-auth/      # Initial RBAC system setup
├── 002-roles-update/      # Role system enhancements
├── 003-audit-system/      # Audit logging improvements
└── templates/             # Migration script templates
```

## Migration Script Naming Convention

Scripts should follow the pattern: `{version}-{description}.sql`

Examples:
- `001-create-auth-tables.sql`
- `002-add-role-permissions.sql`
- `003-update-audit-fields.sql`

## Usage

1. **Development Environment**: Apply scripts manually via YugabyteDB YSQL shell
   ```bash
   ysqlsh -h localhost -p 5433 -U yugabyte -d yugabyte -f migration-script.sql
   ```

2. **Production Environment**: Use automated deployment pipeline
   - Scripts are applied in version order
   - Each script should be idempotent (safe to run multiple times)
   - Include rollback scripts where applicable

## Script Guidelines

- Always include IF NOT EXISTS clauses for CREATE statements
- Use ON CONFLICT DO NOTHING for INSERT statements
- Include comments explaining the purpose of each change
- Test scripts on development environment before production
- Keep scripts atomic - each script should complete a single logical change

## Rollback Strategy

For each migration script, consider creating a corresponding rollback script:
- `001-create-auth-tables.sql` → `rollback-001-create-auth-tables.sql`

## Current Schema

The base authentication schema is defined in `/resources/auth-schema.sql`