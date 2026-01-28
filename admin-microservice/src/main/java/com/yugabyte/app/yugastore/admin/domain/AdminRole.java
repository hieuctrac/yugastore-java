package com.yugabyte.app.yugastore.admin.domain;

/**
 * Admin user roles for role-based access control
 */
public enum AdminRole {
    /**
     * Full access - can create, read, update, delete, and manage users
     */
    ADMIN,

    /**
     * Can create and update products, but cannot delete
     */
    EDITOR,

    /**
     * Read-only access - can view products and audit logs
     */
    VIEWER
}
