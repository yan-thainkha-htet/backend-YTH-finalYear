package com.hospital.irrewaddy.dto;

import com.hospital.irrewaddy.model.User;

public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private User.UserRole role;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String username, String email, User.UserRole role, String message) {
        this.token = token;
        this.username = username;
        this.email = email;
        this.role = role;
        this.message = message;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User.UserRole getRole() {
        return role;
    }

    public void setRole(User.UserRole role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}