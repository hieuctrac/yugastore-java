package com.yugabyte.app.yugastore.admin.dto;

/**
 * DTO for login response
 */
public class LoginResponse {

    private String token;
    private Long expiresIn;
    private AdminUserDto user;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String token, Long expiresIn, AdminUserDto user) {
        this.token = token;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public AdminUserDto getUser() {
        return user;
    }

    public void setUser(AdminUserDto user) {
        this.user = user;
    }
}
