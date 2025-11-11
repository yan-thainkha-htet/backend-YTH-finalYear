package com.hospital.irrewaddy.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;

public class DoctorCompleteProfileRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\w!@#$%^&*()\\-+={}\\[\\]:;\"'<>,.?/|\\\\~`]+$",
            message = "Username must contain only alphanumeric and special characters")
    private String username;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be between 10 and 15 digits")
    private String phone;

    @NotNull(message = "Gender is required")
    private String gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @NotNull(message = "At least one specialization is required")
    @Size(min = 1, message = "At least one specialization is required")
    private Set<Long> specializationIds;

    @NotBlank(message = "Qualification is required")
    private String qualification;
    @NotNull(message = "Experience Years is required")
    private Integer yearOfExperience;
    @NotBlank(message = "Biography is required")
    private String bio;



    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public Set<Long> getSpecializationIds() {
        return specializationIds;
    }

    public void setSpecializationIds(Set<Long> specializationIds) { this.specializationIds = specializationIds; }

    public String getQualification() {
        return qualification;
    }

    public Integer getYearOfExperience() {
        return yearOfExperience;
    }

    public String getBio() {
        return bio;
    }
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public void setYearOfExperience(Integer yearOfExperience) {
        this.yearOfExperience = yearOfExperience;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }
}
