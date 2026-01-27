package security;

import entities.User;
import entities.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT (JSON Web Token) utility class for YugaStore RBAC system.
 *
 * Provides comprehensive JWT operations including:
 * - Token generation with user and role information
 * - Token validation and signature verification
 * - Claims extraction and parsing
 * - Token expiration management
 * - Session tracking integration
 * - Security headers and audit logging support
 *
 * Uses JJWT library with HS256 algorithm for signing.
 * Tokens include user ID, username, roles, and session information.
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    // JWT Configuration
    @Value("${jwt.secret:YugaStore_JWT_Secret_Key_2026_Change_In_Production}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400}") // 24 hours in seconds
    private long jwtExpirationInSeconds;

    @Value("${jwt.refresh-expiration:604800}") // 7 days in seconds
    private long refreshTokenExpirationInSeconds;

    @Value("${jwt.issuer:yugastore-auth}")
    private String jwtIssuer;

    private SecretKey secretKey;

    // JWT Claims
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_SESSION_ID = "sessionId";
    public static final String CLAIM_TOKEN_TYPE = "tokenType";
    public static final String CLAIM_FULL_NAME = "fullName";
    public static final String CLAIM_EMAIL_VERIFIED = "emailVerified";

    // Token Types
    public static final String TOKEN_TYPE_ACCESS = "ACCESS";
    public static final String TOKEN_TYPE_REFRESH = "REFRESH";

    @PostConstruct
    public void init() {
        // Create a secure key from the secret
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT utility initialized with issuer: {}", jwtIssuer);
    }

    // =============================================
    // Token Generation Methods
    // =============================================

    /**
     * Generates an access token for authenticated user.
     */
    public String generateAccessToken(User user, UUID sessionId) {
        return generateToken(user, sessionId, TOKEN_TYPE_ACCESS, jwtExpirationInSeconds);
    }

    /**
     * Generates a refresh token for session management.
     */
    public String generateRefreshToken(User user, UUID sessionId) {
        return generateToken(user, sessionId, TOKEN_TYPE_REFRESH, refreshTokenExpirationInSeconds);
    }

    /**
     * Core method for generating JWT tokens with user information.
     */
    private String generateToken(User user, UUID sessionId, String tokenType, long expirationSeconds) {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(expirationSeconds);

            // Extract role names for claims
            List<String> roles = user.getRoles().stream()
                    .map(Role::getRoleName)
                    .collect(Collectors.toList());

            // Build JWT with comprehensive claims
            String token = Jwts.builder()
                    .setSubject(user.getUserId().toString())
                    .setIssuer(jwtIssuer)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .setId(UUID.randomUUID().toString()) // jti claim for token identification
                    .claim(CLAIM_USER_ID, user.getUserId().toString())
                    .claim(CLAIM_USERNAME, user.getUsername())
                    .claim(CLAIM_EMAIL, user.getEmail())
                    .claim(CLAIM_ROLES, roles)
                    .claim(CLAIM_SESSION_ID, sessionId.toString())
                    .claim(CLAIM_TOKEN_TYPE, tokenType)
                    .claim(CLAIM_FULL_NAME, user.getFullName())
                    .claim(CLAIM_EMAIL_VERIFIED, user.isEmailVerified())
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

            logger.debug("Generated {} token for user: {} (session: {})", tokenType, user.getUsername(), sessionId);
            return token;

        } catch (Exception e) {
            logger.error("Failed to generate JWT token for user: {}", user.getUsername(), e);
            throw new RuntimeException("Token generation failed", e);
        }
    }

    // =============================================
    // Token Validation Methods
    // =============================================

    /**
     * Validates JWT token and returns claims if valid.
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            throw new RuntimeException("Token expired", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT token: {}", e.getMessage());
            throw new RuntimeException("Unsupported token", e);
        } catch (MalformedJwtException e) {
            logger.warn("Malformed JWT token: {}", e.getMessage());
            throw new RuntimeException("Malformed token", e);
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
            throw new RuntimeException("Invalid signature", e);
        } catch (IllegalArgumentException e) {
            logger.warn("JWT claims string is empty: {}", e.getMessage());
            throw new RuntimeException("Empty claims", e);
        } catch (Exception e) {
            logger.error("JWT token validation failed", e);
            throw new RuntimeException("Token validation failed", e);
        }
    }

    /**
     * Checks if token is valid without throwing exceptions.
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Checks if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = validateToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    // =============================================
    // Claims Extraction Methods
    // =============================================

    /**
     * Extracts user ID from token.
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = validateToken(token);
        String userIdStr = claims.get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userIdStr);
    }

    /**
     * Extracts username from token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_USERNAME, String.class);
    }

    /**
     * Extracts email from token.
     */
    public String getEmailFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_EMAIL, String.class);
    }

    /**
     * Extracts roles from token.
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = validateToken(token);
        return (List<String>) claims.get(CLAIM_ROLES);
    }

    /**
     * Extracts session ID from token.
     */
    public UUID getSessionIdFromToken(String token) {
        Claims claims = validateToken(token);
        String sessionIdStr = claims.get(CLAIM_SESSION_ID, String.class);
        return UUID.fromString(sessionIdStr);
    }

    /**
     * Extracts token type from token.
     */
    public String getTokenTypeFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_TOKEN_TYPE, String.class);
    }

    /**
     * Extracts JWT ID (jti claim) from token.
     */
    public String getTokenIdFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.getId();
    }

    /**
     * Extracts full name from token.
     */
    public String getFullNameFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_FULL_NAME, String.class);
    }

    /**
     * Extracts email verification status from token.
     */
    public Boolean getEmailVerifiedFromToken(String token) {
        Claims claims = validateToken(token);
        return claims.get(CLAIM_EMAIL_VERIFIED, Boolean.class);
    }

    /**
     * Gets token expiration date.
     */
    public LocalDateTime getExpirationFromToken(String token) {
        Claims claims = validateToken(token);
        return LocalDateTime.ofInstant(
            claims.getExpiration().toInstant(),
            ZoneId.systemDefault()
        );
    }

    /**
     * Gets token issued date.
     */
    public LocalDateTime getIssuedAtFromToken(String token) {
        Claims claims = validateToken(token);
        return LocalDateTime.ofInstant(
            claims.getIssuedAt().toInstant(),
            ZoneId.systemDefault()
        );
    }

    // =============================================
    // Role and Permission Checking Methods
    // =============================================

    /**
     * Checks if token contains a specific role.
     */
    public boolean hasRole(String token, String roleName) {
        try {
            List<String> roles = getRolesFromToken(token);
            return roles != null && roles.contains(roleName);
        } catch (Exception e) {
            logger.warn("Failed to check role in token", e);
            return false;
        }
    }

    /**
     * Checks if token contains any of the specified roles.
     */
    public boolean hasAnyRole(String token, String... roleNames) {
        try {
            List<String> tokenRoles = getRolesFromToken(token);
            if (tokenRoles == null) return false;

            for (String roleName : roleNames) {
                if (tokenRoles.contains(roleName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn("Failed to check roles in token", e);
            return false;
        }
    }

    /**
     * Checks if token represents an admin user.
     */
    public boolean isAdmin(String token) {
        return hasRole(token, Role.ROLE_ADMIN);
    }

    /**
     * Checks if token represents a support user.
     */
    public boolean isSupport(String token) {
        return hasRole(token, Role.ROLE_SUPPORT);
    }

    /**
     * Checks if token represents a customer.
     */
    public boolean isCustomer(String token) {
        return hasRole(token, Role.ROLE_CUSTOMER);
    }

    // =============================================
    // Token Information Methods
    // =============================================

    /**
     * Gets remaining token validity time in seconds.
     */
    public long getTokenRemainingTime(String token) {
        try {
            Claims claims = validateToken(token);
            long expirationTime = claims.getExpiration().getTime();
            long currentTime = System.currentTimeMillis();
            return Math.max(0, (expirationTime - currentTime) / 1000);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Checks if token needs refresh (expires within threshold).
     */
    public boolean needsRefresh(String token, long thresholdSeconds) {
        return getTokenRemainingTime(token) <= thresholdSeconds;
    }

    /**
     * Extracts all user information from token into a map.
     */
    public UserContext extractUserContext(String token) {
        try {
            Claims claims = validateToken(token);

            UserContext context = new UserContext();
            context.setUserId(UUID.fromString(claims.get(CLAIM_USER_ID, String.class)));
            context.setUsername(claims.get(CLAIM_USERNAME, String.class));
            context.setEmail(claims.get(CLAIM_EMAIL, String.class));
            context.setFullName(claims.get(CLAIM_FULL_NAME, String.class));
            context.setEmailVerified(claims.get(CLAIM_EMAIL_VERIFIED, Boolean.class));
            context.setRoles((List<String>) claims.get(CLAIM_ROLES));
            context.setSessionId(UUID.fromString(claims.get(CLAIM_SESSION_ID, String.class)));
            context.setTokenId(claims.getId());
            context.setTokenType(claims.get(CLAIM_TOKEN_TYPE, String.class));

            return context;

        } catch (Exception e) {
            logger.error("Failed to extract user context from token", e);
            throw new RuntimeException("Failed to extract user context", e);
        }
    }

    // =============================================
    // Utility Methods
    // =============================================

    /**
     * Generates a new session-bound access token from refresh token.
     */
    public String refreshAccessToken(String refreshToken) {
        try {
            // Validate refresh token
            Claims claims = validateToken(refreshToken);
            String tokenType = claims.get(CLAIM_TOKEN_TYPE, String.class);

            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                throw new RuntimeException("Invalid token type for refresh");
            }

            // Extract user information
            UUID userId = UUID.fromString(claims.get(CLAIM_USER_ID, String.class));
            UUID sessionId = UUID.fromString(claims.get(CLAIM_SESSION_ID, String.class));

            // Note: In a full implementation, you'd reload the user from database
            // to ensure current roles and status. For now, we reuse claims.

            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(jwtExpirationInSeconds);

            return Jwts.builder()
                    .setSubject(userId.toString())
                    .setIssuer(jwtIssuer)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .setId(UUID.randomUUID().toString())
                    .claim(CLAIM_USER_ID, claims.get(CLAIM_USER_ID))
                    .claim(CLAIM_USERNAME, claims.get(CLAIM_USERNAME))
                    .claim(CLAIM_EMAIL, claims.get(CLAIM_EMAIL))
                    .claim(CLAIM_ROLES, claims.get(CLAIM_ROLES))
                    .claim(CLAIM_SESSION_ID, sessionId.toString())
                    .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                    .claim(CLAIM_FULL_NAME, claims.get(CLAIM_FULL_NAME))
                    .claim(CLAIM_EMAIL_VERIFIED, claims.get(CLAIM_EMAIL_VERIFIED))
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();

        } catch (Exception e) {
            logger.error("Failed to refresh access token", e);
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * User context extracted from JWT token.
     */
    public static class UserContext {
        private UUID userId;
        private String username;
        private String email;
        private String fullName;
        private Boolean emailVerified;
        private List<String> roles;
        private UUID sessionId;
        private String tokenId;
        private String tokenType;

        // Getters and setters
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public Boolean getEmailVerified() { return emailVerified; }
        public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }

        public UUID getSessionId() { return sessionId; }
        public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

        public String getTokenId() { return tokenId; }
        public void setTokenId(String tokenId) { this.tokenId = tokenId; }

        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }

        public boolean hasRole(String roleName) {
            return roles != null && roles.contains(roleName);
        }

        public boolean hasAnyRole(String... roleNames) {
            if (roles == null) return false;
            for (String roleName : roleNames) {
                if (roles.contains(roleName)) return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "UserContext{" +
                    "userId=" + userId +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    ", roles=" + roles +
                    ", sessionId=" + sessionId +
                    ", tokenType='" + tokenType + '\'' +
                    '}';
        }
    }
}