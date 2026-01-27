package security;

import entities.Role;
import entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic unit tests for JWT utility functionality.
 * Tests core JWT operations without Spring dependencies.
 */
class JwtUtilBasicTest {

    private JwtUtil jwtUtil;
    private User testUser;
    private UUID testSessionId;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();

        // Set test configuration using reflection
        setField(jwtUtil, "jwtSecret",
            "YugaStore_Test_JWT_Secret_Key_For_Unit_Testing_256bit_minimum_length");
        setField(jwtUtil, "jwtExpirationInSeconds", 3600L);
        setField(jwtUtil, "refreshTokenExpirationInSeconds", 86400L);
        setField(jwtUtil, "jwtIssuer", "yugastore-test");

        // Initialize the JWT utility
        jwtUtil.init();

        // Create test user
        testUser = new User("testuser", "test@example.com", "hashedpassword");
        testUser.setUserId(UUID.randomUUID());
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        // Add roles to user
        Role customerRole = Role.createCustomerRole();
        customerRole.setRoleId(UUID.randomUUID());
        testUser.addRole(customerRole);

        testSessionId = UUID.randomUUID();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void testGenerateAndValidateAccessToken() {
        System.out.println("🧪 Testing JWT token generation and validation...");

        // Generate token
        String token = jwtUtil.generateAccessToken(testUser, testSessionId);

        // Validate basic properties
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isEmpty(), "Token should not be empty");
        assertTrue(token.contains("."), "Token should be in JWT format (with dots)");
        assertEquals(3, token.split("\\.").length, "JWT should have 3 parts (header.payload.signature)");

        System.out.println("✅ Generated token: " + token.substring(0, 30) + "...");

        // Validate token
        assertDoesNotThrow(() -> {
            var claims = jwtUtil.validateToken(token);
            assertNotNull(claims, "Claims should not be null");
            assertEquals(testUser.getUserId().toString(), claims.getSubject(), "Subject should match user ID");
        });

        System.out.println("✅ Token validation successful");
    }

    @Test
    void testExtractClaimsFromToken() {
        System.out.println("🧪 Testing JWT claims extraction...");

        String token = jwtUtil.generateAccessToken(testUser, testSessionId);

        // Test user ID extraction
        UUID extractedUserId = jwtUtil.getUserIdFromToken(token);
        assertEquals(testUser.getUserId(), extractedUserId, "User ID should match");
        System.out.println("✅ User ID extraction: " + extractedUserId);

        // Test username extraction
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        assertEquals(testUser.getUsername(), extractedUsername, "Username should match");
        System.out.println("✅ Username extraction: " + extractedUsername);

        // Test email extraction
        String extractedEmail = jwtUtil.getEmailFromToken(token);
        assertEquals(testUser.getEmail(), extractedEmail, "Email should match");
        System.out.println("✅ Email extraction: " + extractedEmail);

        // Test roles extraction
        List<String> extractedRoles = jwtUtil.getRolesFromToken(token);
        assertNotNull(extractedRoles, "Roles should not be null");
        assertFalse(extractedRoles.isEmpty(), "Roles should not be empty");
        assertTrue(extractedRoles.contains(Role.ROLE_CUSTOMER), "Should contain CUSTOMER role");
        System.out.println("✅ Roles extraction: " + extractedRoles);

        // Test session ID extraction
        UUID extractedSessionId = jwtUtil.getSessionIdFromToken(token);
        assertEquals(testSessionId, extractedSessionId, "Session ID should match");
        System.out.println("✅ Session ID extraction: " + extractedSessionId);
    }

    @Test
    void testRoleChecking() {
        System.out.println("🧪 Testing JWT role checking...");

        String token = jwtUtil.generateAccessToken(testUser, testSessionId);

        // Test individual role checking
        assertTrue(jwtUtil.hasRole(token, Role.ROLE_CUSTOMER), "Should have CUSTOMER role");
        assertFalse(jwtUtil.hasRole(token, Role.ROLE_ADMIN), "Should not have ADMIN role");
        assertFalse(jwtUtil.hasRole(token, Role.ROLE_SUPPORT), "Should not have SUPPORT role");
        System.out.println("✅ Individual role checking successful");

        // Test multiple role checking
        assertTrue(jwtUtil.hasAnyRole(token, Role.ROLE_CUSTOMER, Role.ROLE_ADMIN),
                  "Should have at least CUSTOMER role");
        assertFalse(jwtUtil.hasAnyRole(token, Role.ROLE_ADMIN, Role.ROLE_SUPPORT),
                   "Should not have ADMIN or SUPPORT roles");
        System.out.println("✅ Multiple role checking successful");

        // Test convenience methods
        assertTrue(jwtUtil.isCustomer(token), "Should identify as customer");
        assertFalse(jwtUtil.isAdmin(token), "Should not identify as admin");
        assertFalse(jwtUtil.isSupport(token), "Should not identify as support");
        System.out.println("✅ Convenience role methods successful");
    }

    @Test
    void testUserContextExtraction() {
        System.out.println("🧪 Testing user context extraction...");

        String token = jwtUtil.generateAccessToken(testUser, testSessionId);

        JwtUtil.UserContext context = jwtUtil.extractUserContext(token);

        assertNotNull(context, "User context should not be null");
        assertEquals(testUser.getUserId(), context.getUserId(), "User ID should match");
        assertEquals(testUser.getUsername(), context.getUsername(), "Username should match");
        assertEquals(testUser.getEmail(), context.getEmail(), "Email should match");
        assertEquals(testSessionId, context.getSessionId(), "Session ID should match");
        assertTrue(context.hasRole(Role.ROLE_CUSTOMER), "Should have CUSTOMER role in context");

        System.out.println("✅ User context extraction successful: " + context.toString());
    }

    @Test
    void testTokenValidity() {
        System.out.println("🧪 Testing token validity checking...");

        String validToken = jwtUtil.generateAccessToken(testUser, testSessionId);
        String invalidToken = "invalid.jwt.token";

        // Test valid token
        assertTrue(jwtUtil.isTokenValid(validToken), "Valid token should return true");
        System.out.println("✅ Valid token recognized");

        // Test invalid token
        assertFalse(jwtUtil.isTokenValid(invalidToken), "Invalid token should return false");
        System.out.println("✅ Invalid token rejected");
    }

    @Test
    void testTokenRemainingTime() {
        System.out.println("🧪 Testing token remaining time calculation...");

        String token = jwtUtil.generateAccessToken(testUser, testSessionId);
        long remainingTime = jwtUtil.getTokenRemainingTime(token);

        assertTrue(remainingTime > 3500, "Should have more than 3500 seconds remaining, got: " + remainingTime);
        assertTrue(remainingTime <= 3600, "Should have at most 3600 seconds remaining, got: " + remainingTime);

        System.out.println("✅ Token remaining time: " + remainingTime + " seconds");
    }

    @Test
    void testTokenRefresh() {
        System.out.println("🧪 Testing token refresh functionality...");

        String refreshToken = jwtUtil.generateRefreshToken(testUser, testSessionId);

        // Verify refresh token type
        String tokenType = jwtUtil.getTokenTypeFromToken(refreshToken);
        assertEquals("REFRESH", tokenType, "Should be a refresh token");
        System.out.println("✅ Refresh token generated with correct type");

        // Test refreshing access token
        String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);
        assertNotNull(newAccessToken, "New access token should not be null");

        String newTokenType = jwtUtil.getTokenTypeFromToken(newAccessToken);
        assertEquals("ACCESS", newTokenType, "Refreshed token should be access token");

        // Verify user data is preserved
        UUID newUserId = jwtUtil.getUserIdFromToken(newAccessToken);
        assertEquals(testUser.getUserId(), newUserId, "User ID should be preserved");

        System.out.println("✅ Token refresh successful");
    }

    @Test
    void testGenerateRefreshToken() {
        System.out.println("🧪 Testing refresh token generation...");

        String refreshToken = jwtUtil.generateRefreshToken(testUser, testSessionId);

        assertNotNull(refreshToken, "Refresh token should not be null");
        assertFalse(refreshToken.isEmpty(), "Refresh token should not be empty");

        // Verify it's a different format/content than access token
        String accessToken = jwtUtil.generateAccessToken(testUser, testSessionId);
        assertNotEquals(accessToken, refreshToken, "Refresh token should differ from access token");

        System.out.println("✅ Refresh token generation successful");
    }

    @Test
    void testInvalidTokenHandling() {
        System.out.println("🧪 Testing invalid token handling...");

        String[] invalidTokens = {
            "invalid.jwt.token",
            "not-a-jwt-at-all",
            "",
            null
        };

        for (String invalidToken : invalidTokens) {
            if (invalidToken == null) continue;

            assertThrows(RuntimeException.class, () -> {
                jwtUtil.validateToken(invalidToken);
            }, "Invalid token should throw exception: " + invalidToken);

            assertFalse(jwtUtil.isTokenValid(invalidToken), "Invalid token should return false");
        }

        System.out.println("✅ Invalid token handling successful");
    }
}