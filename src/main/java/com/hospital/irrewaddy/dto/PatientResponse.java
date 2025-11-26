package com.hospital.irrewaddy.dto;


import com.hospital.irrewaddy.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    public String bloodGroup;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private User.Gender gender;
    private boolean isActive;
    private int age;
    // Constructors
    public PatientResponse() {
    }
}