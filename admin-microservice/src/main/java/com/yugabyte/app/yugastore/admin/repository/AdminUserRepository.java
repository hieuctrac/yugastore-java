package com.yugabyte.app.yugastore.admin.repository;

import com.yugabyte.app.yugastore.admin.domain.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AdminUser entities
 * Uses Spring Data JPA for YSQL access
 */
@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, UUID> {

    /**
     * Find an admin user by username
     *
     * @param username the username to search for
     * @return Optional containing the user if found
     */
    Optional<AdminUser> findByUsername(String username);

    /**
     * Find an active admin user by username
     *
     * @param username the username to search for
     * @param isActive the active status
     * @return Optional containing the user if found
     */
    Optional<AdminUser> findByUsernameAndIsActive(String username, Boolean isActive);

    /**
     * Check if a username already exists
     *
     * @param username the username to check
     * @return true if username exists
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email already exists
     *
     * @param email the email to check
     * @return true if email exists
     */
    boolean existsByEmail(String email);
}
