package com.yugabyte.app.yugastore.admin.service;

import com.yugabyte.app.yugastore.admin.domain.AdminUser;
import com.yugabyte.app.yugastore.admin.dto.LoginRequest;
import com.yugabyte.app.yugastore.admin.dto.LoginResponse;

import java.util.Optional;
import java.util.UUID;

/**
 * Service interface for admin user operations
 */
public interface AdminUserService {

    /**
     * Authenticate a user and generate JWT token
     *
     * @param loginRequest the login credentials
     * @return LoginResponse with JWT token and user details
     */
    LoginResponse authenticate(LoginRequest loginRequest);

    /**
     * Find a user by username
     *
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<AdminUser> findByUsername(String username);

    /**
     * Find a user by ID
     *
     * @param userId the user ID
     * @return Optional containing the user if found
     */
    Optional<AdminUser> findById(UUID userId);

    /**
     * Update user's last login timestamp
     *
     * @param userId the user ID
     */
    void updateLastLogin(UUID userId);

    /**
     * Record failed login attempt
     *
     * @param username the username
     */
    void recordFailedLoginAttempt(String username);

    /**
     * Check if user account is locked
     *
     * @param username the username
     * @return true if account is locked
     */
    boolean isAccountLocked(String username);
}
