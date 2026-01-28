package com.yugabyte.app.yugastore.admin.service.impl;

import com.yugabyte.app.yugastore.admin.domain.AdminUser;
import com.yugabyte.app.yugastore.admin.domain.AuditActionType;
import com.yugabyte.app.yugastore.admin.domain.ProductAuditLog;
import com.yugabyte.app.yugastore.admin.dto.AuditLogDto;
import com.yugabyte.app.yugastore.admin.repository.AdminUserRepository;
import com.yugabyte.app.yugastore.admin.repository.ProductAuditLogRepository;
import com.yugabyte.app.yugastore.admin.service.AdminAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AdminAuditService
 */
@Service
public class AdminAuditServiceImpl implements AdminAuditService {

    private static final Logger logger = LoggerFactory.getLogger(AdminAuditServiceImpl.class);

    @Autowired
    private ProductAuditLogRepository auditLogRepository;

    @Autowired
    private AdminUserRepository adminUserRepository;

    // Cache for username lookups to reduce database queries
    private final Map<UUID, String> usernameCache = new HashMap<>();

    @Override
    @Transactional
    public ProductAuditLog logAction(
            String productAsin,
            UUID userId,
            AuditActionType actionType,
            String fieldName,
            String oldValue,
            String newValue,
            String reason,
            String ipAddress) {

        ProductAuditLog log = ProductAuditLog.builder()
                .productAsin(productAsin)
                .userId(userId)
                .actionType(actionType)
                .fieldName(fieldName)
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .ipAddress(ipAddress)
                .build();

        ProductAuditLog savedLog = auditLogRepository.save(log);

        logger.info("Audit log created: user={}, action={}, product={}, field={}",
                userId, actionType, productAsin, fieldName);

        return savedLog;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getProductHistory(String productAsin, Pageable pageable) {
        Page<ProductAuditLog> logs = auditLogRepository.findByProductAsinOrderByTimestampDesc(productAsin, pageable);
        return logs.map(log -> AuditLogDto.fromEntity(log, getUsername(log.getUserId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogDto> getAuditLogs(
            String productAsin,
            UUID userId,
            AuditActionType actionType,
            Instant fromDate,
            Instant toDate,
            Pageable pageable) {

        Page<ProductAuditLog> logs = auditLogRepository.findWithFilters(
                productAsin, userId, actionType, fromDate, toDate, pageable);

        return logs.map(log -> AuditLogDto.fromEntity(log, getUsername(log.getUserId())));
    }

    @Override
    @Transactional
    public int deleteOldLogs(Instant cutoffDate) {
        int deleted = auditLogRepository.deleteByTimestampBefore(cutoffDate);
        logger.info("Deleted {} old audit logs before {}", deleted, cutoffDate);
        return deleted;
    }

    /**
     * Scheduled job to delete audit logs older than 90 days
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupOldAuditLogs() {
        Instant cutoffDate = Instant.now().minus(90, ChronoUnit.DAYS);
        int deleted = deleteOldLogs(cutoffDate);
        logger.info("Audit log cleanup completed: {} logs deleted", deleted);
    }

    /**
     * Get username for a user ID (with caching)
     */
    private String getUsername(UUID userId) {
        return usernameCache.computeIfAbsent(userId, id ->
                adminUserRepository.findById(id)
                        .map(AdminUser::getUsername)
                        .orElse("Unknown")
        );
    }

    /**
     * Clear username cache (called periodically to prevent memory issues)
     */
    @Scheduled(fixedRate = 3600000) // Every hour
    public void clearUsernameCache() {
        usernameCache.clear();
        logger.debug("Username cache cleared");
    }
}
