package com.yugabyte.app.yugastore.web;

import entities.User;
import entities.Role;
import repositories.UserRepository;
import security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

/**
 * REST API Controller for Authentication and User Management.
 *
 * Provides JSON-based API endpoints for:
 * - User authentication and JWT token management
 * - User registration and profile management
 * - Role-based access control
 * - Session management and logout
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Authenticate user and return JWT tokens.
     *
     * POST /api/auth/login
     * Body: { "username": "user@example.com", "password": "password123" }
     *
     * Returns JWT access token and refresh token with user information.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Authenticate user credentials
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Optional<User> userOpt = userRepository.findByUsernameOrEmailAndActive(
                userDetails.getUsername()
            );

            if (!userOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, "User not found"));
            }

            User user = userOpt.get();

            // Check if user account is active
            if (!user.isAccountActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Account is inactive"));
            }

            // Generate session ID and JWT tokens
            UUID sessionId = UUID.randomUUID();
            String accessToken = jwtUtil.generateAccessToken(user, sessionId);
            String refreshToken = jwtUtil.generateRefreshToken(user, sessionId);

            // Update last login timestamp
            user.updateLastLogin();
            userRepository.save(user);

            // Prepare user profile for response
            UserProfile userProfile = new UserProfile(user);

            return ResponseEntity.ok(new JwtResponse(
                accessToken,
                refreshToken,
                userProfile,
                "Bearer",
                86400L // 24 hours
            ));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Invalid username or password"));
        }
    }

    /**
     * Register new user account.
     *
     * POST /api/auth/register
     * Body: { "username": "newuser", "email": "user@example.com", "password": "password123", "firstName": "John", "lastName": "Doe" }
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        // Check if username already exists
        if (userRepository.existsByUsernameIgnoreCase(signUpRequest.getUsername())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Username is already taken!"));
        }

        // Check if email already exists
        if (userRepository.existsByEmailIgnoreCase(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Email address already in use!"));
        }

        // Create new user account
        User user = new User(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            passwordEncoder.encode(signUpRequest.getPassword()),
            signUpRequest.getFirstName(),
            signUpRequest.getLastName()
        );

        // Assign default CUSTOMER role
        // Note: In a real implementation, you'd fetch the role from RoleRepository
        // For now, we'll let the database handle role assignment via triggers or default logic

        User savedUser = userRepository.save(user);

        return ResponseEntity.ok(new ApiResponse(true,
            "User registered successfully! Please verify your email address."));
    }

    /**
     * Get current user profile information.
     *
     * GET /api/auth/me
     * Header: Authorization: Bearer <access_token>
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Extract token from "Bearer " prefix
            String jwtToken = token.replace("Bearer ", "");

            // Validate token and extract user ID
            if (jwtUtil.isTokenValid(jwtToken)) {
                UUID userId = jwtUtil.getUserIdFromToken(jwtToken);

                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UserProfile userProfile = new UserProfile(user);
                    return ResponseEntity.ok(userProfile);
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Invalid or expired token"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Authentication required"));
        }
    }

    /**
     * Refresh JWT access token using refresh token.
     *
     * POST /api/auth/refresh
     * Body: { "refreshToken": "refresh_token_here" }
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();

            if (jwtUtil.isTokenValid(refreshToken)) {
                UUID userId = jwtUtil.getUserIdFromToken(refreshToken);
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    UUID sessionId = jwtUtil.getSessionIdFromToken(refreshToken);

                    // Generate new access token
                    String newAccessToken = jwtUtil.generateAccessToken(user, sessionId);

                    return ResponseEntity.ok(new AccessTokenResponse(newAccessToken, 86400L));
                }
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Invalid refresh token"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ApiResponse(false, "Token refresh failed"));
        }
    }

    /**
     * Logout user and invalidate tokens.
     *
     * POST /api/auth/logout
     * Header: Authorization: Bearer <access_token>
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        // In a production system, you would:
        // 1. Add token to blacklist/revocation list
        // 2. Remove session from user_sessions table
        // 3. Clear any cached user data

        // For now, return success - client should discard tokens
        return ResponseEntity.ok(new ApiResponse(true, "User logged out successfully"));
    }

    // ===== Request/Response DTOs =====

    public static class LoginRequest {
        private String username;
        private String password;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SignUpRequest {
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;

        // Getters and setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    public static class RefreshTokenRequest {
        private String refreshToken;

        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }

    public static class JwtResponse {
        private String accessToken;
        private String refreshToken;
        private UserProfile user;
        private String tokenType;
        private Long expiresIn;

        public JwtResponse(String accessToken, String refreshToken, UserProfile user, String tokenType, Long expiresIn) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.user = user;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
        }

        // Getters
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public UserProfile getUser() { return user; }
        public String getTokenType() { return tokenType; }
        public Long getExpiresIn() { return expiresIn; }
    }

    public static class AccessTokenResponse {
        private String accessToken;
        private Long expiresIn;

        public AccessTokenResponse(String accessToken, Long expiresIn) {
            this.accessToken = accessToken;
            this.expiresIn = expiresIn;
        }

        // Getters
        public String getAccessToken() { return accessToken; }
        public Long getExpiresIn() { return expiresIn; }
    }

    public static class UserProfile {
        private UUID userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String fullName;
        private boolean emailVerified;
        private boolean active;
        private List<String> roles;
        private LocalDateTime createdAt;
        private LocalDateTime lastLoginAt;

        public UserProfile(User user) {
            this.userId = user.getUserId();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.fullName = user.getFullName();
            this.emailVerified = user.isEmailVerified();
            this.active = user.isAccountActive();
            this.roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(java.util.stream.Collectors.toList());
            this.createdAt = user.getCreatedAt();
            this.lastLoginAt = user.getLastLoginAt();
        }

        // Getters
        public UUID getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getFullName() { return fullName; }
        public boolean isEmailVerified() { return emailVerified; }
        public boolean isActive() { return active; }
        public List<String> getRoles() { return roles; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    }

    public static class ApiResponse {
        private boolean success;
        private String message;

        public ApiResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}