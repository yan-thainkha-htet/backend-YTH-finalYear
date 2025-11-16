package com.hospital.irrewaddy.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PatientResponse {
    private Long id;
    public String bloodGroup;

    // Constructors
    public PatientResponse() {
    }
}