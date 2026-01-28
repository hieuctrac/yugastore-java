package com.yugabyte.app.yugastore.admin.domain;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Product audit log entity for tracking all product changes
 * Stored in YugabyteDB YSQL (product_audit_log table)
 */
@Entity
@Table(name = "product_audit_log")
public class ProductAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "product_asin", nullable = false, length = 50)
    private String productAsin;

    @Column(name = "user_id", nullable = false, columnDefinition = "UUID")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private AuditActionType actionType;

    @Column(name = "field_name", length = 100)
    private String fieldName;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }

    // Constructors
    public ProductAuditLog() {
    }

    public ProductAuditLog(String productAsin, UUID userId, AuditActionType actionType) {
        this.productAsin = productAsin;
        this.userId = userId;
        this.actionType = actionType;
        this.timestamp = Instant.now();
    }

    // Builder pattern for easier construction
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ProductAuditLog log = new ProductAuditLog();

        public Builder productAsin(String productAsin) {
            log.productAsin = productAsin;
            return this;
        }

        public Builder userId(UUID userId) {
            log.userId = userId;
            return this;
        }

        public Builder actionType(AuditActionType actionType) {
            log.actionType = actionType;
            return this;
        }

        public Builder fieldName(String fieldName) {
            log.fieldName = fieldName;
            return this;
        }

        public Builder oldValue(String oldValue) {
            log.oldValue = oldValue;
            return this;
        }

        public Builder newValue(String newValue) {
            log.newValue = newValue;
            return this;
        }

        public Builder reason(String reason) {
            log.reason = reason;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            log.timestamp = timestamp;
            return this;
        }

        public ProductAuditLog build() {
            if (log.timestamp == null) {
                log.timestamp = Instant.now();
            }
            return log;
        }
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
