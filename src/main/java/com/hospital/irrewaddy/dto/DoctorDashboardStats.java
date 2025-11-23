package com.hospital.irrewaddy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DoctorDashboardStats {
    private long todayTotal;
    private long thisWeekTotal;
    private long pending;
    private long totalPatients;
    private long experienceYears;
    private String specialization;
    private String department;
}
