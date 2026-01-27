package repositories;

import entities.User;
import entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity operations.
 * Provides comprehensive data access methods for authentication,
 * user management, and RBAC system operations.
 *
 * This repository supports:
 * - Authentication (login by username/email)
 * - User registration and profile management
 * - Role-based queries for authorization
 * - Account status and email verification
 * - User search and filtering for support/admin operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // =============================================
    // Authentication Methods
    // =============================================

    /**
     * Finds a user by username for authentication.
     * Only returns active users.
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isActive = true")
    Optional<User> findByUsernameAndActive(@Param("username") String username);

    /**
     * Finds a user by email for authentication.
     * Only returns active users.
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findByEmailAndActive(@Param("email") String email);

    /**
     * Finds a user by username or email for flexible authentication.
     * Only returns active users.
     */
    @Query("SELECT u FROM User u WHERE (u.username = :login OR u.email = :login) AND u.isActive = true")
    Optional<User> findByUsernameOrEmailAndActive(@Param("login") String login);

    // =============================================
    // User Existence Checks
    // =============================================

    /**
     * Checks if a username already exists (case-insensitive).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    boolean existsByUsernameIgnoreCase(@Param("username") String username);

    /**
     * Checks if an email already exists (case-insensitive).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    /**
     * Checks if username exists for a different user (for profile updates).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.username) = LOWER(:username) AND u.userId != :userId")
    boolean existsByUsernameIgnoreCaseAndUserIdNot(@Param("username") String username, @Param("userId") UUID userId);

    /**
     * Checks if email exists for a different user (for profile updates).
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email) AND u.userId != :userId")
    boolean existsByEmailIgnoreCaseAndUserIdNot(@Param("email") String email, @Param("userId") UUID userId);

    // =============================================
    // Role-Based Queries
    // =============================================

    /**
     * Finds users with a specific role.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName AND u.isActive = true")
    List<User> findByRoleName(@Param("roleName") String roleName);

    /**
     * Finds users with any of the specified roles.
     */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.roleName IN :roleNames AND u.isActive = true")
    List<User> findByRoleNameIn(@Param("roleNames") List<String> roleNames);

    /**
     * Finds users with a specific role and email verification status.
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.roleName = :roleName AND u.isEmailVerified = :isVerified AND u.isActive = true")
    List<User> findByRoleNameAndEmailVerified(@Param("roleName") String roleName, @Param("isVerified") boolean isVerified);

    /**
     * Checks if a user has a specific role.
     */
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.roles r WHERE u.userId = :userId AND r.roleName = :roleName")
    boolean hasRole(@Param("userId") UUID userId, @Param("roleName") String roleName);

    // =============================================
    // Account Status Queries
    // =============================================

    /**
     * Finds all active users.
     */
    List<User> findByIsActiveTrue();

    /**
     * Finds all inactive users.
     */
    List<User> findByIsActiveFalse();

    /**
     * Finds users with unverified emails.
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.isActive = true")
    List<User> findUnverifiedUsers();

    /**
     * Finds users created within a date range.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Finds users who haven't logged in since a specific date.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :lastLoginThreshold OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsersSince(@Param("lastLoginThreshold") LocalDateTime lastLoginThreshold);

    // =============================================
    // Search and Filter Methods (for Support/Admin)
    // =============================================

    /**
     * Searches users by name or email (case-insensitive).
     * Used by support and admin interfaces.
     */
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND u.isActive = true " +
           "ORDER BY u.createdAt DESC")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);

    /**
     * Finds users by email domain (for organizational management).
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE CONCAT('%@', :domain) AND u.isActive = true")
    List<User> findByEmailDomain(@Param("domain") String domain);

    /**
     * Finds users with incomplete profiles (missing first/last name).
     */
    @Query("SELECT u FROM User u WHERE (u.firstName IS NULL OR u.lastName IS NULL) AND u.isActive = true")
    List<User> findUsersWithIncompleteProfiles();

    // =============================================
    // Statistical Queries
    // =============================================

    /**
     * Counts users by role.
     */
    @Query("SELECT r.roleName, COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE u.isActive = true GROUP BY r.roleName")
    List<Object[]> countUsersByRole();

    /**
     * Counts active users.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = true")
    long countActiveUsers();

    /**
     * Counts users registered today.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE DATE(u.createdAt) = CURRENT_DATE")
    long countUsersRegisteredToday();

    /**
     * Counts users with verified emails.
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isEmailVerified = true AND u.isActive = true")
    long countVerifiedUsers();

    // =============================================
    // Admin Management Queries
    // =============================================

    /**
     * Finds users who need admin attention (unverified for too long).
     */
    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false AND u.createdAt < :thresholdDate AND u.isActive = true")
    List<User> findUsersNeedingAdminAttention(@Param("thresholdDate") LocalDateTime thresholdDate);

    /**
     * Finds recently registered users for welcome campaigns.
     */
    @Query("SELECT u FROM User u WHERE u.createdAt > :sinceDate AND u.isActive = true ORDER BY u.createdAt DESC")
    List<User> findRecentlyRegisteredUsers(@Param("sinceDate") LocalDateTime sinceDate);

    /**
     * Finds users with multiple failed login attempts (security monitoring).
     * Note: This would typically join with an audit table, but for now we use last login time.
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL AND u.createdAt < :oldEnoughDate AND u.isActive = true")
    List<User> findUsersWhoNeverLoggedIn(@Param("oldEnoughDate") LocalDateTime oldEnoughDate);

    // =============================================
    // Custom Update Methods
    // =============================================

    /**
     * Updates user's last login time.
     */
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.userId = :userId")
    void updateLastLoginTime(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);

    /**
     * Marks user email as verified.
     */
    @Query("UPDATE User u SET u.isEmailVerified = true WHERE u.userId = :userId")
    void markEmailAsVerified(@Param("userId") UUID userId);

    /**
     * Deactivates a user account.
     */
    @Query("UPDATE User u SET u.isActive = false WHERE u.userId = :userId")
    void deactivateUser(@Param("userId") UUID userId);

    /**
     * Activates a user account.
     */
    @Query("UPDATE User u SET u.isActive = true WHERE u.userId = :userId")
    void activateUser(@Param("userId") UUID userId);
}