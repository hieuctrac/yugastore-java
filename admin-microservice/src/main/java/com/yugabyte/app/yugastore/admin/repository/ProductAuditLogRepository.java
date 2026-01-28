package com.yugabyte.app.yugastore.admin.repository;

import com.yugabyte.app.yugastore.admin.domain.AuditActionType;
import com.yugabyte.app.yugastore.admin.domain.ProductAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ProductAuditLog entities
 * Uses Spring Data JPA for YSQL access
 */
@Repository
public interface ProductAuditLogRepository extends JpaRepository<ProductAuditLog, Long> {

    /**
     * Find audit logs for a specific product
     *
     * @param productAsin the product ASIN
     * @param pageable pagination parameters
     * @return Page of audit logs
     */
    Page<ProductAuditLog> findByProductAsinOrderByTimestampDesc(String productAsin, Pageable pageable);

    /**
     * Find audit logs by user
     *
     * @param userId the user ID
     * @param pageable pagination parameters
     * @return Page of audit logs
     */
    Page<ProductAuditLog> findByUserIdOrderByTimestampDesc(UUID userId, Pageable pageable);

    /**
     * Find audit logs by action type
     *
     * @param actionType the action type
     * @param pageable pagination parameters
     * @return Page of audit logs
     */
    Page<ProductAuditLog> findByActionTypeOrderByTimestampDesc(AuditActionType actionType, Pageable pageable);

    /**
     * Find audit logs within a date range
     *
     * @param fromDate start date
     * @param toDate end date
     * @param pageable pagination parameters
     * @return Page of audit logs
     */
    @Query("SELECT a FROM ProductAuditLog a WHERE a.timestamp BETWEEN :fromDate AND :toDate ORDER BY a.timestamp DESC")
    Page<ProductAuditLog> findByTimestampBetween(
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        Pageable pageable
    );

    /**
     * Find audit logs with multiple filters
     *
     * @param productAsin optional product ASIN filter
     * @param userId optional user ID filter
     * @param actionType optional action type filter
     * @param fromDate optional start date filter
     * @param toDate optional end date filter
     * @param pageable pagination parameters
     * @return Page of audit logs
     */
    @Query("SELECT a FROM ProductAuditLog a WHERE " +
           "(:productAsin IS NULL OR a.productAsin = :productAsin) AND " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:actionType IS NULL OR a.actionType = :actionType) AND " +
           "(:fromDate IS NULL OR a.timestamp >= :fromDate) AND " +
           "(:toDate IS NULL OR a.timestamp <= :toDate) " +
           "ORDER BY a.timestamp DESC")
    Page<ProductAuditLog> findWithFilters(
        @Param("productAsin") String productAsin,
        @Param("userId") UUID userId,
        @Param("actionType") AuditActionType actionType,
        @Param("fromDate") Instant fromDate,
        @Param("toDate") Instant toDate,
        Pageable pageable
    );

    /**
     * Delete old audit logs (for retention policy)
     *
     * @param cutoffDate the cutoff date (logs older than this will be deleted)
     * @return number of logs deleted
     */
    @Query("DELETE FROM ProductAuditLog a WHERE a.timestamp < :cutoffDate")
    int deleteByTimestampBefore(@Param("cutoffDate") Instant cutoffDate);
}
