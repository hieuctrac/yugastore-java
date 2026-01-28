package com.yugabyte.app.yugastore.admin.dto;

import com.yugabyte.app.yugastore.admin.domain.AdminRole;
import com.yugabyte.app.yugastore.admin.domain.AdminUser;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for admin user information (without sensitive data)
 */
public class AdminUserDto {

    private UUID userId;
    private String username;
    private AdminRole role;
    private String email;
    private Instant createdAt;
    private Instant lastLogin;

    // Constructors
    public AdminUserDto() {
    }

    public AdminUserDto(UUID userId, String username, AdminRole role, String email, Instant createdAt, Instant lastLogin) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.email = email;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    /**
     * Create DTO from entity
     */
    public static AdminUserDto fromEntity(AdminUser user) {
        return new AdminUserDto(
            user.getUserId(),
            user.getUsername(),
            user.getRole(),
            user.getEmail(),
            user.getCreatedAt(),
            user.getLastLogin()
        );
    }

    // Getters and Setters
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

    public AdminRole getRole() {
        return role;
    }

    public void setRole(AdminRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }
}
