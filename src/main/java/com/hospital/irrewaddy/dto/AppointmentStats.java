package com.hospital.irrewaddy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStats {
    private long todayTotal;
    private long pending;
    private long confirmed;
    private long completed;
    private long noShow;
}
