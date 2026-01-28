package com.yugabyte.app.yugastore.admin.service;

import com.yugabyte.app.yugastore.admin.domain.AuditActionType;
import com.yugabyte.app.yugastore.admin.domain.ProductAuditLog;
import com.yugabyte.app.yugastore.admin.dto.AuditLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

/**
 * Service interface for audit logging operations
 */
public interface AdminAuditService {

    /**
     * Log an action performed on a product
     *
     * @param productAsin the product ASIN
     * @param userId the user who performed the action
     * @param actionType the type of action
     * @param fieldName the field that was changed (null for CREATE/DELETE)
     * @param oldValue the old value (null for CREATE)
     * @param newValue the new value (null for DELETE)
     * @param reason optional reason for the action
     * @param ipAddress the IP address of the request
     * @return the created audit log entry
     */
    ProductAuditLog logAction(
        String productAsin,
        UUID userId,
        AuditActionType actionType,
        String fieldName,
        String oldValue,
        String newValue,
        String reason,
        String ipAddress
    );

    /**
     * Get audit history for a specific product
     *
     * @param productAsin the product ASIN
     * @param pageable pagination parameters
     * @return Page of audit log DTOs
     */
    Page<AuditLogDto> getProductHistory(String productAsin, Pageable pageable);

    /**
     * Get audit logs with filters
     *
     * @param productAsin optional product ASIN filter
     * @param userId optional user ID filter
     * @param actionType optional action type filter
     * @param fromDate optional start date filter
     * @param toDate optional end date filter
     * @param pageable pagination parameters
     * @return Page of audit log DTOs
     */
    Page<AuditLogDto> getAuditLogs(
        String productAsin,
        UUID userId,
        AuditActionType actionType,
        Instant fromDate,
        Instant toDate,
        Pageable pageable
    );

    /**
     * Delete audit logs older than the specified date
     * (Used for retention policy enforcement)
     *
     * @param cutoffDate the cutoff date
     * @return number of logs deleted
     */
    int deleteOldLogs(Instant cutoffDate);
}
