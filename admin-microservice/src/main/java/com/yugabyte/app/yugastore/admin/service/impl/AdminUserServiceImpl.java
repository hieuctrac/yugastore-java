package com.yugabyte.app.yugastore.admin.service.impl;

import com.yugabyte.app.yugastore.admin.domain.AdminUser;
import com.yugabyte.app.yugastore.admin.dto.AdminUserDto;
import com.yugabyte.app.yugastore.admin.dto.LoginRequest;
import com.yugabyte.app.yugastore.admin.dto.LoginResponse;
import com.yugabyte.app.yugastore.admin.repository.AdminUserRepository;
import com.yugabyte.app.yugastore.admin.service.AdminUserService;
import com.yugabyte.app.yugastore.admin.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AdminUserService
 * Also implements UserDetailsService for Spring Security integration
 */
@Service
public class AdminUserServiceImpl implements AdminUserService, UserDetailsService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${admin.max-login-attempts:5}")
    private int maxLoginAttempts;

    @Value("${admin.lockout-duration:300}")
    private int lockoutDurationSeconds;

    @Override
    @Transactional
    public LoginResponse authenticate(LoginRequest loginRequest) {
        // Find user
        AdminUser user = adminUserRepository.findByUsernameAndIsActive(loginRequest.getUsername(), true)
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        // Check if account is locked
        if (user.isAccountLocked()) {
            throw new BadCredentialsException("Account is temporarily locked due to too many failed login attempts");
        }

        // Verify password
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            recordFailedLoginAttempt(loginRequest.getUsername());
            throw new BadCredentialsException("Invalid username or password");
        }

        // Reset failed login attempts and update last login
        user.resetFailedLoginAttempts();
        user.setLastLogin(Instant.now());
        adminUserRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // Create response
        AdminUserDto userDto = AdminUserDto.fromEntity(user);
        return new LoginResponse(token, jwtUtil.getExpirationInSeconds(), userDto);
    }

    @Override
    public Optional<AdminUser> findByUsername(String username) {
        return adminUserRepository.findByUsername(username);
    }

    @Override
    public Optional<AdminUser> findById(UUID userId) {
        return adminUserRepository.findById(userId);
    }

    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        adminUserRepository.findById(userId).ifPresent(user -> {
            user.setLastLogin(Instant.now());
            adminUserRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void recordFailedLoginAttempt(String username) {
        adminUserRepository.findByUsername(username).ifPresent(user -> {
            user.incrementFailedLoginAttempts();

            if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
                user.lockAccount(lockoutDurationSeconds);
            }

            adminUserRepository.save(user);
        });
    }

    @Override
    public boolean isAccountLocked(String username) {
        return adminUserRepository.findByUsername(username)
                .map(AdminUser::isAccountLocked)
                .orElse(false);
    }

    /**
     * Load user by username for Spring Security
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser user = adminUserRepository.findByUsernameAndIsActive(username, true)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        return new User(
                user.getUsername(),
                user.getPasswordHash(),
                user.getIsActive(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !user.isAccountLocked(), // accountNonLocked
                authorities
        );
    }
}
