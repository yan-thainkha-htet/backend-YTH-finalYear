package com.hospital.irrewaddy.dto;

public class CreateDoctorResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    public String departmentName;
    private String message;

    public CreateDoctorResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getRole() { return role; }

    public void setRole(String role) { this.role = role; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getMessage() { return message; }

    public void setMessage(String message) { this.message = message; }
}
