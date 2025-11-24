package com.hospital.irrewaddy.dto;

import com.hospital.irrewaddy.model.User;

import java.math.BigDecimal;
import java.util.List;

public class DoctorResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private User.Gender gender;
    private String specialization;
    private String qualification;
    private Integer experienceYears;
    private String bio;
    private Float rating;
    private Integer totalPatients;
    private String departmentName;
    private Boolean isActive;
    private String message;
    private List<DoctorAvailabilityResponse> availability;

    // Constructors
    public DoctorResponse() {
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public List<DoctorAvailabilityResponse> getAvailability() {
        return availability;
    }

    public void setAvailability(List<DoctorAvailabilityResponse> availability) {
        this.availability = availability;
    }

    public DoctorResponse(Long id, Long userId, String username, String fullName, String email,
                          String phone, User.Gender gender, String specialization, String qualification,
                          Integer experienceYears, String bio,
                          Float rating, Integer totalPatients, String departmentName,
                          Boolean isActive, String message) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.bio = bio;
        this.rating = rating;
        this.totalPatients = totalPatients;
        this.departmentName = departmentName;
        this.isActive = isActive;
        this.message = message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public User.Gender getGender(){ return gender; }

    public void setGender(User.Gender gender) {
        this.gender = gender;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public Integer getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Float getRating() {
        return rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public Integer getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(Integer totalPatients) {
        this.totalPatients = totalPatients;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}