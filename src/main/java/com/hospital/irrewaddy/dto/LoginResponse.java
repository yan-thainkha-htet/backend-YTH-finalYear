package com.hospital.irrewaddy.dto;

public class LoginResponse {
    private String token;
    private String username;
    private String role;
    private Boolean mustChangePassword; // Add this field
    private String message;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String token, String username, String role, Boolean mustChangePassword, String message) {
        this.token = token;
        this.username = username;
        this.role = role;
        this.mustChangePassword = mustChangePassword;
        this.message = message;
    }

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Boolean mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}