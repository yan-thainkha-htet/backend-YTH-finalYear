package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.AppointmentResponse;
import com.hospital.irrewaddy.dto.DashboardStatsResponse;
import com.hospital.irrewaddy.dto.SuperAdminDashboardData;
import com.hospital.irrewaddy.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hospital/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Get comprehensive dashboard statistics
     * Accessible by Super Admin and Admin
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getDashboardStats() {
        try {
            DashboardStatsResponse stats = dashboardService.getDashboardStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching dashboard statistics: " + e.getMessage());
        }
    }

    /**
     * Get recent appointments (last N appointments)
     * Default limit: 10
     */
    @GetMapping("/recent-appointments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getRecentAppointments(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            List<AppointmentResponse> appointments = dashboardService.getRecentAppointments(limit);
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching recent appointments: " + e.getMessage());
        }
    }

    /**
     * Get today's appointments
     */
    @GetMapping("/today-appointments")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getTodayAppointments() {
        try {
            List<AppointmentResponse> appointments = dashboardService.getTodayAppointments();
            return ResponseEntity.ok(appointments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching today's appointments: " + e.getMessage());
        }
    }

    /**
     * Get appointment status breakdown
     */
    @GetMapping("/appointment-status-breakdown")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getAppointmentStatusBreakdown() {
        try {
            Map<String, Integer> breakdown = dashboardService.getAppointmentStatusBreakdown();
            return ResponseEntity.ok(breakdown);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching appointment status breakdown: " + e.getMessage());
        }
    }

    /**
     * Get user statistics by role
     */
    @GetMapping("/user-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getUserStatsByRole() {
        try {
            Map<String, Long> stats = dashboardService.getUserStatsByRole();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching user statistics: " + e.getMessage());
        }
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<?> getDashboardByAdminId(@PathVariable Long id) {
        try {
            SuperAdminDashboardData data = dashboardService.getDashboardByAdminId(id);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    /**
     * Get active users count by role
     */
    @GetMapping("/active-user-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getActiveUsersStatsByRole() {
        try {
            Map<String, Long> stats = dashboardService.getActiveUsersStatsByRole();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching active user statistics: " + e.getMessage());
        }
    }

    /**
     * Get appointment statistics for a date range
     */
    @GetMapping("/appointment-stats-by-date")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getAppointmentStatsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            Map<String, Object> stats = dashboardService.getAppointmentStatsByDateRange(startDate, endDate);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching appointment statistics: " + e.getMessage());
        }
    }

    /**
     * Get department-wise statistics
     */
    @GetMapping("/department-stats")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getDepartmentStats() {
        try {
            List<Map<String, Object>> stats = dashboardService.getDepartmentStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching department statistics: " + e.getMessage());
        }
    }
}