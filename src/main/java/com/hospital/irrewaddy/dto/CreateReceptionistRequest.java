package com.hospital.irrewaddy.dto;

import com.hospital.irrewaddy.model.Receptionist;
import com.hospital.irrewaddy.model.User;
import jakarta.validation.constraints.*;

public class CreateReceptionistRequest {



    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;




    @NotNull(message = "Shift is required")
    private Receptionist.Shift shift;

    private Integer deskNumber;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[\\w!@#$%^&*()\\-+={}\\[\\]:;\"'<>,.?/|\\\\~`]+$",
            message = "Username must contain only alphanumeric and special characters")
    private String username;
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    private String password;

    private Boolean isActive = true;

    // Constructors
    public CreateReceptionistRequest() {
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Receptionist.Shift getShift() {
        return shift;
    }

    public void setShift(Receptionist.Shift shift) {
        this.shift = shift;
    }

    public Integer getDeskNumber() {
        return deskNumber;
    }

    public void setDeskNumber(Integer deskNumber) {
        this.deskNumber = deskNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}