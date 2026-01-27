package security;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * UserContext model for API Gateway microservice.
 *
 * This class carries authenticated user information through the gateway
 * for authorization decisions and request routing. It's designed to be:
 * - Lightweight for performance in gateway operations
 * - Thread-safe for concurrent request handling
 * - Serializable for inter-service communication
 * - Immutable to prevent security vulnerabilities
 *
 * The UserContext is populated from JWT tokens and passed to downstream
 * microservices via HTTP headers for RBAC enforcement.
 */
public class UserContext {

    private final UUID userId;
    private final String username;
    private final String email;
    private final String fullName;
    private final List<String> roles;
    private final boolean emailVerified;
    private final UUID sessionId;
    private final String tokenId;
    private final boolean anonymous;

    // Role constants for convenience
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";
    public static final String ROLE_CUSTOMER = "ROLE_CUSTOMER";
    public static final String ROLE_SUPPORT = "ROLE_SUPPORT";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // Private constructor to enforce builder pattern
    private UserContext(Builder builder) {
        this.userId = builder.userId;
        this.username = builder.username;
        this.email = builder.email;
        this.fullName = builder.fullName;
        this.roles = Collections.unmodifiableList(builder.roles);
        this.emailVerified = builder.emailVerified;
        this.sessionId = builder.sessionId;
        this.tokenId = builder.tokenId;
        this.anonymous = builder.anonymous;
    }

    // =============================================
    // Factory Methods
    // =============================================

    /**
     * Creates an anonymous user context for unauthenticated requests.
     */
    public static UserContext anonymous() {
        return new Builder()
                .anonymous(true)
                .roles(Collections.singletonList(ROLE_ANONYMOUS))
                .build();
    }

    /**
     * Creates an authenticated user context from JWT claims.
     */
    public static UserContext authenticated(UUID userId, String username, String email,
                                          String fullName, List<String> roles,
                                          boolean emailVerified, UUID sessionId, String tokenId) {
        return new Builder()
                .userId(userId)
                .username(username)
                .email(email)
                .fullName(fullName)
                .roles(roles)
                .emailVerified(emailVerified)
                .sessionId(sessionId)
                .tokenId(tokenId)
                .anonymous(false)
                .build();
    }

    // =============================================
    // Getters
    // =============================================

    public UUID getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public String getTokenId() {
        return tokenId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isAuthenticated() {
        return !anonymous;
    }

    // =============================================
    // Role Checking Methods
    // =============================================

    /**
     * Checks if user has a specific role.
     */
    public boolean hasRole(String roleName) {
        return roles != null && roles.contains(roleName);
    }

    /**
     * Checks if user has any of the specified roles.
     */
    public boolean hasAnyRole(String... roleNames) {
        if (roles == null) return false;

        for (String roleName : roleNames) {
            if (roles.contains(roleName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if user has all the specified roles.
     */
    public boolean hasAllRoles(String... roleNames) {
        if (roles == null) return false;

        for (String roleName : roleNames) {
            if (!roles.contains(roleName)) {
                return false;
            }
        }
        return true;
    }

    // =============================================
    // Permission Methods for API Gateway
    // =============================================

    /**
     * Checks if user is an administrator.
     */
    public boolean isAdmin() {
        return hasRole(ROLE_ADMIN);
    }

    /**
     * Checks if user is support staff.
     */
    public boolean isSupport() {
        return hasRole(ROLE_SUPPORT);
    }

    /**
     * Checks if user is a customer.
     */
    public boolean isCustomer() {
        return hasRole(ROLE_CUSTOMER);
    }

    /**
     * Checks if user can access admin endpoints.
     */
    public boolean canAccessAdmin() {
        return isAdmin();
    }

    /**
     * Checks if user can access support endpoints.
     */
    public boolean canAccessSupport() {
        return isAdmin() || isSupport();
    }

    /**
     * Checks if user can access customer endpoints.
     */
    public boolean canAccessCustomer() {
        return isAuthenticated() && (isAdmin() || isSupport() || isCustomer());
    }

    /**
     * Checks if user can access public endpoints (everyone can).
     */
    public boolean canAccessPublic() {
        return true;
    }

    /**
     * Checks if user can access product browsing (anonymous + authenticated).
     */
    public boolean canAccessProducts() {
        return true; // All users can browse products
    }

    /**
     * Checks if user can access cart operations.
     */
    public boolean canAccessCart() {
        return isAuthenticated(); // Only authenticated users can use cart
    }

    /**
     * Checks if user can access checkout operations.
     */
    public boolean canAccessCheckout() {
        return isAuthenticated(); // Only authenticated users can checkout
    }

    /**
     * Checks if user can access specific user data (for data isolation).
     */
    public boolean canAccessUserData(UUID targetUserId) {
        // Admins and support can access any user data
        if (isAdmin() || isSupport()) {
            return true;
        }

        // Users can only access their own data
        return isAuthenticated() && userId != null && userId.equals(targetUserId);
    }

    // =============================================
    // HTTP Header Generation
    // =============================================

    /**
     * Generates HTTP headers for downstream microservice calls.
     * These headers propagate user context through the microservices.
     */
    public static class Headers {
        public static final String USER_ID = "X-User-Id";
        public static final String USERNAME = "X-Username";
        public static final String EMAIL = "X-User-Email";
        public static final String ROLES = "X-User-Roles";
        public static final String SESSION_ID = "X-Session-Id";
        public static final String TOKEN_ID = "X-Token-Id";
        public static final String EMAIL_VERIFIED = "X-Email-Verified";
        public static final String IS_ANONYMOUS = "X-Is-Anonymous";
    }

    /**
     * Converts user context to HTTP headers for microservice communication.
     */
    public java.util.Map<String, String> toHeaders() {
        java.util.Map<String, String> headers = new java.util.HashMap<>();

        if (!isAnonymous()) {
            headers.put(Headers.USER_ID, userId.toString());
            headers.put(Headers.USERNAME, username);
            headers.put(Headers.EMAIL, email);
            headers.put(Headers.SESSION_ID, sessionId.toString());
            headers.put(Headers.TOKEN_ID, tokenId);
            headers.put(Headers.EMAIL_VERIFIED, String.valueOf(emailVerified));
        }

        headers.put(Headers.ROLES, String.join(",", roles));
        headers.put(Headers.IS_ANONYMOUS, String.valueOf(isAnonymous()));

        return headers;
    }

    // =============================================
    // Builder Pattern
    // =============================================

    public static class Builder {
        private UUID userId;
        private String username;
        private String email;
        private String fullName;
        private List<String> roles = Collections.emptyList();
        private boolean emailVerified = false;
        private UUID sessionId;
        private String tokenId;
        private boolean anonymous = true;

        public Builder userId(UUID userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder roles(List<String> roles) {
            this.roles = roles != null ? roles : Collections.emptyList();
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder sessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public Builder anonymous(boolean anonymous) {
            this.anonymous = anonymous;
            return this;
        }

        public UserContext build() {
            return new UserContext(this);
        }
    }

    // =============================================
    // Object Methods
    // =============================================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        UserContext that = (UserContext) obj;

        if (isAnonymous() && that.isAnonymous()) return true;

        return userId != null && userId.equals(that.userId) &&
               sessionId != null && sessionId.equals(that.sessionId);
    }

    @Override
    public int hashCode() {
        if (isAnonymous()) return 0;
        return java.util.Objects.hash(userId, sessionId);
    }

    @Override
    public String toString() {
        if (isAnonymous()) {
            return "UserContext{anonymous=true, roles=" + roles + "}";
        }

        return "UserContext{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles +
                ", emailVerified=" + emailVerified +
                ", sessionId=" + sessionId +
                ", anonymous=" + anonymous +
                '}';
    }

    /**
     * Creates a sanitized version for logging (removes sensitive information).
     */
    public String toLogString() {
        if (isAnonymous()) {
            return "UserContext{anonymous=true}";
        }

        return "UserContext{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", emailVerified=" + emailVerified +
                ", anonymous=" + anonymous +
                '}';
    }
}