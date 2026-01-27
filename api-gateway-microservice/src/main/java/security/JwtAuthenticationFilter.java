package security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for API Gateway.
 *
 * This filter processes incoming HTTP requests to:
 * 1. Extract and validate JWT tokens from Authorization header
 * 2. Create UserContext from valid tokens
 * 3. Set up Spring Security authentication context
 * 4. Add user context headers for downstream microservices
 * 5. Handle anonymous users for public endpoints
 * 6. Provide audit logging for authentication events
 *
 * The filter runs once per request and is essential for RBAC enforcement
 * across all YugaStore microservices.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    // HTTP Headers
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // JWT Configuration (should match login microservice)
    @Value("${jwt.secret:YugaStore_RBAC_JWT_Secret_2026_CHANGE_IN_PRODUCTION_256bit_minimum}")
    private String jwtSecret;

    @Value("${jwt.issuer:yugastore-auth}")
    private String jwtIssuer;

    private SecretKey secretKey;

    // Paths that don't require authentication
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/products",           // Product browsing
        "/api/public",            // Public endpoints
        "/health",                // Health checks
        "/actuator",              // Actuator endpoints
        "/auth/login",            // Login endpoint
        "/auth/register",         // Registration endpoint
        "/auth/forgot-password",  // Password reset
        "/swagger",               // API documentation
        "/v3/api-docs"           // OpenAPI docs
    );

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        logger.info("JWT Authentication Filter initialized");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();

        logger.debug("Processing request: {} {}", method, requestPath);

        try {
            // Check if this is a public endpoint
            if (isPublicPath(requestPath)) {
                logger.debug("Public endpoint accessed: {}", requestPath);
                handleAnonymousUser(request);
            } else {
                // Extract and validate JWT token
                String token = extractTokenFromRequest(request);

                if (token != null) {
                    handleAuthenticatedUser(request, token);
                } else {
                    // No token provided for protected endpoint
                    logger.warn("No JWT token provided for protected endpoint: {}", requestPath);
                    handleAuthenticationFailure(response, "Authentication required");
                    return;
                }
            }

            // Continue the filter chain
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.error("Authentication filter error for request {}: {}", requestPath, e.getMessage(), e);
            handleAuthenticationFailure(response, "Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Extracts JWT token from Authorization header.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
            logger.debug("JWT token extracted from Authorization header");
            return token;
        }

        // Also check for token in query parameter (for WebSocket connections)
        String tokenParam = request.getParameter("token");
        if (tokenParam != null && !tokenParam.isEmpty()) {
            logger.debug("JWT token extracted from query parameter");
            return tokenParam;
        }

        return null;
    }

    /**
     * Validates JWT token and creates user context.
     */
    private UserContext validateTokenAndCreateContext(String token) {
        try {
            // Parse and validate JWT token
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .requireIssuer(jwtIssuer)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Extract user information from claims
            UUID userId = UUID.fromString(claims.get("userId", String.class));
            String username = claims.get("username", String.class);
            String email = claims.get("email", String.class);
            String fullName = claims.get("fullName", String.class);
            Boolean emailVerified = claims.get("emailVerified", Boolean.class);
            UUID sessionId = UUID.fromString(claims.get("sessionId", String.class));
            String tokenId = claims.getId();

            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("roles");

            // Create authenticated user context
            UserContext userContext = UserContext.authenticated(
                userId, username, email, fullName, roles,
                emailVerified != null && emailVerified,
                sessionId, tokenId
            );

            logger.debug("Successfully validated JWT token for user: {}", username);
            return userContext;

        } catch (Exception e) {
            logger.warn("JWT token validation failed: {}", e.getMessage());
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    /**
     * Handles authenticated user requests.
     */
    private void handleAuthenticatedUser(HttpServletRequest request, String token) {
        UserContext userContext = validateTokenAndCreateContext(token);

        // Set up Spring Security context
        List<SimpleGrantedAuthority> authorities = userContext.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                userContext,
                null,
                authorities
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Add user context headers for downstream services
        addUserContextHeaders(request, userContext);

        logger.debug("Authenticated user: {} with roles: {}",
                    userContext.getUsername(), userContext.getRoles());
    }

    /**
     * Handles anonymous user requests for public endpoints.
     */
    private void handleAnonymousUser(HttpServletRequest request) {
        UserContext anonymousContext = UserContext.anonymous();

        // Set up anonymous Spring Security context
        List<SimpleGrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority(UserContext.ROLE_ANONYMOUS)
        );

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                anonymousContext,
                null,
                authorities
            );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Add anonymous user headers
        addUserContextHeaders(request, anonymousContext);

        logger.debug("Anonymous user accessing public endpoint");
    }

    /**
     * Adds user context headers to request for downstream microservices.
     */
    private void addUserContextHeaders(HttpServletRequest request, UserContext userContext) {
        // Create a wrapper to add headers
        RequestWrapper wrapper = new RequestWrapper(request);

        // Add user context headers
        userContext.toHeaders().forEach(wrapper::addHeader);

        logger.debug("Added user context headers for downstream services");
    }

    /**
     * Handles authentication failures.
     */
    private void handleAuthenticationFailure(HttpServletResponse response, String message) throws IOException {
        SecurityContextHolder.clearContext();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
            "{\"error\":\"Authentication Failed\",\"message\":\"%s\",\"timestamp\":\"%s\"}",
            message,
            java.time.Instant.now().toString()
        );

        response.getWriter().write(jsonResponse);
        response.getWriter().flush();

        logger.warn("Authentication failed: {}", message);
    }

    /**
     * Checks if the request path is public (doesn't require authentication).
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    /**
     * Request wrapper to add headers for downstream services.
     */
    private static class RequestWrapper extends javax.servlet.http.HttpServletRequestWrapper {
        private final java.util.Map<String, String> customHeaders = new java.util.HashMap<>();

        public RequestWrapper(HttpServletRequest request) {
            super(request);
        }

        public void addHeader(String name, String value) {
            customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String customValue = customHeaders.get(name);
            if (customValue != null) {
                return customValue;
            }
            return super.getHeader(name);
        }

        @Override
        public java.util.Enumeration<String> getHeaderNames() {
            java.util.Set<String> names = new java.util.HashSet<>();

            // Add original headers
            java.util.Enumeration<String> originalNames = super.getHeaderNames();
            while (originalNames.hasMoreElements()) {
                names.add(originalNames.nextElement());
            }

            // Add custom headers
            names.addAll(customHeaders.keySet());

            return java.util.Collections.enumeration(names);
        }

        @Override
        public java.util.Enumeration<String> getHeaders(String name) {
            String customValue = customHeaders.get(name);
            if (customValue != null) {
                return java.util.Collections.enumeration(java.util.Collections.singletonList(customValue));
            }
            return super.getHeaders(name);
        }
    }
}

/**
 * Configuration class for JWT Authentication Filter.
 */
@org.springframework.context.annotation.Configuration
class JwtAuthenticationConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationConfig.class);

    @org.springframework.context.annotation.Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        logger.info("Configuring JWT Authentication Filter");
        return new JwtAuthenticationFilter();
    }
}