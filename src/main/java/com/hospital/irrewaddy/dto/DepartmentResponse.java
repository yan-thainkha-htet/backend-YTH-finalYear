package com.hospital.irrewaddy.dto;

import java.time.LocalDateTime;

public class DepartmentResponse {
    private Long id;
    private String name;
    private String description;
    private String departmentHeadName;
    private Long departmentHeadId;
    private Integer totalDoctors;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String message;

    // Constructors
    public DepartmentResponse() {
    }

    public DepartmentResponse(Long id, String name, String description, Integer capacity,
                              String departmentHeadName, Long departmentHeadId, Integer totalDoctors,
                              Boolean isActive, LocalDateTime createdAt, String message) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.departmentHeadName = departmentHeadName;
        this.departmentHeadId = departmentHeadId;
        this.totalDoctors = totalDoctors;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartmentHeadName() {
        return departmentHeadName;
    }

    public void setDepartmentHeadName(String departmentHeadName) {
        this.departmentHeadName = departmentHeadName;
    }

    public Long getDepartmentHeadId() {
        return departmentHeadId;
    }

    public void setDepartmentHeadId(Long departmentHeadId) {
        this.departmentHeadId = departmentHeadId;
    }

    public Integer getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(Integer totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}