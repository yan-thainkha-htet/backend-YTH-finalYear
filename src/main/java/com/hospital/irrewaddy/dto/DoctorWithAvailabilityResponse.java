package com.hospital.irrewaddy.dto;

import com.hospital.irrewaddy.dto.DoctorAvailabilityResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWithAvailabilityResponse {

    private Long doctorId;
    private String fullName;
    private String email;
    private String specialization;
    private String departmentName;
    private String qualification;
    private int experienceYears;
    private List<DoctorAvailabilityResponse> availability;
}
