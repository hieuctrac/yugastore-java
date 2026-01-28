package com.yugabyte.app.yugastore.admin.domain;

/**
 * Types of actions that can be audited in the product audit log
 */
public enum AuditActionType {
    /**
     * Product creation
     */
    CREATE,

    /**
     * Product field update
     */
    UPDATE,

    /**
     * Product permanent deletion
     */
    DELETE,

    /**
     * Product soft deletion (deactivation)
     */
    DEACTIVATE,

    /**
     * Product reactivation
     */
    ACTIVATE,

    /**
     * Bulk operation affecting multiple products
     */
    BULK_UPDATE
}
