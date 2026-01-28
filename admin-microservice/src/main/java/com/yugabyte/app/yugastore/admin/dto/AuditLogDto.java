package com.yugabyte.app.yugastore.admin.dto;

import com.yugabyte.app.yugastore.admin.domain.AuditActionType;
import com.yugabyte.app.yugastore.admin.domain.ProductAuditLog;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for audit log entries
 */
public class AuditLogDto {

    private Long auditId;
    private String productAsin;
    private UUID userId;
    private String username;
    private AuditActionType actionType;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String reason;
    private String ipAddress;
    private Instant timestamp;

    // Constructors
    public AuditLogDto() {
    }

    /**
     * Create DTO from entity
     */
    public static AuditLogDto fromEntity(ProductAuditLog log, String username) {
        AuditLogDto dto = new AuditLogDto();
        dto.setAuditId(log.getAuditId());
        dto.setProductAsin(log.getProductAsin());
        dto.setUserId(log.getUserId());
        dto.setUsername(username);
        dto.setActionType(log.getActionType());
        dto.setFieldName(log.getFieldName());
        dto.setOldValue(log.getOldValue());
        dto.setNewValue(log.getNewValue());
        dto.setReason(log.getReason());
        dto.setIpAddress(log.getIpAddress());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }

    // Getters and Setters
    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getProductAsin() {
        return productAsin;
    }

    public void setProductAsin(String productAsin) {
        this.productAsin = productAsin;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuditActionType getActionType() {
        return actionType;
    }

    public void setActionType(AuditActionType actionType) {
        this.actionType = actionType;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
