package com.hospital.irrewaddy.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDepartmentRequest {

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Min(value = 0, message = "Capacity cannot be negative")
    private Integer capacity;

    private Long departmentHeadId; // Doctor ID who is head of department (optional)

    private Boolean isActive = true; // Default to active

    // Constructors
    public CreateDepartmentRequest() {
    }

    public CreateDepartmentRequest(String name, String description, Long departmentHeadId, Integer capacity,
                                   Boolean isActive) {
        this.name = name;
        this.description = description;
        this.departmentHeadId = departmentHeadId;
        this.capacity = capacity;
        this.isActive = isActive;
    }

    // Getters and Setters
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

    public Integer getCapacity(){
        return capacity;
    }

    public void setCapacity(Integer capacity){
        this.capacity = capacity;
    }
    public Long getDepartmentHeadId() {
        return departmentHeadId;
    }

    public void setDepartmentHeadId(Long departmentHeadId) {
        this.departmentHeadId = departmentHeadId;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}