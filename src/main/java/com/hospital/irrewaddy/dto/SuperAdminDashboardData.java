package com.hospital.irrewaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDashboardData {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;
    private long todayAppointments;
}
