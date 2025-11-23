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
public class AppointmentStats {
    private long todayTotal;
    private long pending;
    private long confirmed;
    private long completed;
    private long noShow;
    private long upcoming;
    private long cancelled;
    private List<AppointmentResponse> upcomingAppointments;

    public AppointmentStats(long todayAppointment, long pendingCount, long confirmedCount, long completedCount, long cancelledCount) {
    }
}
