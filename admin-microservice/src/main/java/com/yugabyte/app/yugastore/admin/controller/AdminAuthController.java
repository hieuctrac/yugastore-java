package com.yugabyte.app.yugastore.admin.controller;

import com.yugabyte.app.yugastore.admin.dto.AdminUserDto;
import com.yugabyte.app.yugastore.admin.dto.LoginRequest;
import com.yugabyte.app.yugastore.admin.dto.LoginResponse;
import com.yugabyte.app.yugastore.admin.service.AdminUserService;
import com.yugabyte.app.yugastore.admin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller for admin authentication endpoints
 */
@RestController
@RequestMapping("/api/admin/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
public class AdminAuthController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = adminUserService.authenticate(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout endpoint (client-side token removal, server just confirms)
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Clear security context
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    public ResponseEntity<AdminUserDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return adminUserService.findByUsername(username)
                .map(AdminUserDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Password reset request endpoint (placeholder for future implementation)
     */
    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");

        // TODO: Implement password reset functionality
        // 1. Validate email exists
        // 2. Generate reset token
        // 3. Send email with reset link
        // 4. Store reset token with expiration

        Map<String, String> response = new HashMap<>();
        response.put("message", "If the email exists, a password reset link has been sent");
        return ResponseEntity.ok(response);
    }
}
