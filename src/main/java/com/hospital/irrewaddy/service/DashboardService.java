package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.Appointment;
import com.hospital.irrewaddy.model.Doctor;
import com.hospital.irrewaddy.model.Specialization;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AppointmentService appointmentService;

    /**
     * Get comprehensive dashboard statistics
     */
    public DashboardStatsResponse getDashboardStats() {
        DashboardStatsResponse stats = new DashboardStatsResponse();

        // Total counts
        stats.setTotalPatients(patientRepository.count());
        stats.setTotalDoctors(doctorRepository.count());
        stats.setTotalReceptionists(receptionistRepository.count());
        stats.setTotalAdmins(countUsersByRole(User.UserRole.ADMIN));
        stats.setTotalSuperAdmins(countUsersByRole(User.UserRole.SUPER_ADMIN));
        stats.setTotalAppointments(appointmentRepository.count());
        stats.setTotalDepartments(departmentRepository.count());

        // Today's appointments
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = appointmentRepository.findByAppointmentDate(today);
        stats.setTodayAppointments(todayAppointments.size());

        // Completed appointments today
        long completedToday = todayAppointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED)
                .count();
        stats.setTodayCompletedAppointments((int) completedToday);

        // Appointment status breakdown
        Map<String, Integer> statusBreakdown = getAppointmentStatusBreakdown();
        stats.setAppointmentStatusBreakdown(statusBreakdown);

        // Growth percentages (compared to last month)
        stats.setPatientsGrowthPercent(calculateGrowthPercent("patients"));
        stats.setAppointmentsGrowthPercent(calculateGrowthPercent("appointments"));
        stats.setDoctorsAddedThisMonth(calculateNewThisMonth("doctors"));

        return stats;
    }

    /**
     * Get appointment status breakdown
     */
    public Map<String, Integer> getAppointmentStatusBreakdown() {
        Map<String, Integer> breakdown = new HashMap<>();

        for (Appointment.AppointmentStatus status : Appointment.AppointmentStatus.values()) {
            int count = appointmentRepository.findByStatus(status).size();
            breakdown.put(status.name(), count);
        }

        return breakdown;
    }

    /**
     * Get recent appointments (last N appointments)
     */
    public List<AppointmentResponse> getRecentAppointments(int limit) {
        List<Appointment> appointments = appointmentRepository.findAll();

        // Sort by created date descending and limit
        return appointments.stream()
                .sorted((a1, a2) -> a2.getCreatedAt().compareTo(a1.getCreatedAt()))
                .limit(limit)
                .map(apt -> convertToAppointmentResponse(apt))
                .collect(Collectors.toList());
    }

    /**
     * Get today's appointments
     */
    public List<AppointmentResponse> getTodayAppointments() {
        LocalDate today = LocalDate.now();
        List<Appointment> appointments = appointmentRepository.findByAppointmentDate(today);

        return appointments.stream()
                .map(apt -> convertToAppointmentResponse(apt))
                .collect(Collectors.toList());
    }

    /**
     * Get user statistics by role
     */
    public Map<String, Long> getUserStatsByRole() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("SUPER_ADMIN", countUsersByRole(User.UserRole.SUPER_ADMIN));
        stats.put("ADMIN", countUsersByRole(User.UserRole.ADMIN));
        stats.put("DOCTOR", (long) doctorRepository.findAll().size());
        stats.put("RECEPTIONIST", (long) receptionistRepository.findAll().size());
        stats.put("PATIENT", (long) patientRepository.findAll().size());

        return stats;
    }


    /**
     * Get active users count by role
     */
    public Map<String, Long> getActiveUsersStatsByRole() {
        Map<String, Long> stats = new HashMap<>();

        stats.put("SUPER_ADMIN", countActiveUsersByRole(User.UserRole.SUPER_ADMIN));
        stats.put("ADMIN", countActiveUsersByRole(User.UserRole.ADMIN));
        stats.put("DOCTOR", (long) doctorRepository.findByUser_IsActiveTrue().size());
        stats.put("RECEPTIONIST", (long) receptionistRepository.findByUser_IsActiveTrue().size());
        stats.put("PATIENT", patientRepository.findAll().stream()
                .filter(p -> p.getUser().getIsActive())
                .count());

        return stats;
    }

    /**
     * Get appointment statistics for a specific date range
     */
    public Map<String, Object> getAppointmentStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        List<Appointment> appointments = appointmentRepository.findAll().stream()
                .filter(apt -> !apt.getAppointmentDate().isBefore(startDate)
                        && !apt.getAppointmentDate().isAfter(endDate))
                .collect(Collectors.toList());

        stats.put("totalAppointments", appointments.size());
        stats.put("completed", appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.COMPLETED).count());
        stats.put("pending", appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.PENDING).count());
        stats.put("confirmed", appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CONFIRMED).count());
        stats.put("cancelled", appointments.stream()
                .filter(apt -> apt.getStatus() == Appointment.AppointmentStatus.CANCELLED).count());

        return stats;
    }

    /**
     * Get department-wise statistics
     */
    public List<Map<String, Object>> getDepartmentStats() {
        return departmentRepository.findAll().stream()
                .map(dept -> {
                    Map<String, Object> deptStats = new HashMap<>();
                    deptStats.put("id", dept.getId());
                    deptStats.put("name", dept.getName());
                    deptStats.put("totalDoctors", dept.getDoctors() != null ? dept.getDoctors().size() : 0);
                    deptStats.put("totalAppointments", dept.getAppointments() != null ? dept.getAppointments().size() : 0);
                    deptStats.put("isActive", dept.getIsActive());
                    return deptStats;
                })
                .collect(Collectors.toList());
    }

    // Helper methods

    private long countUsersByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .count();
    }

    private long countActiveUsersByRole(User.UserRole role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role && user.getIsActive())
                .count();
    }

    private double calculateGrowthPercent(String entity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonth = now.minusMonths(1);

        if (entity.equals("patients")) {
            long currentCount = patientRepository.count();
            long lastMonthCount = patientRepository.findAll().stream()
                    .filter(p -> p.getUser().getCreatedAt().isBefore(lastMonth))
                    .count();

            if (lastMonthCount == 0) return 0;
            return ((double) (currentCount - lastMonthCount) / lastMonthCount) * 100;
        } else if (entity.equals("appointments")) {
            long currentCount = appointmentRepository.count();
            long lastMonthCount = appointmentRepository.findAll().stream()
                    .filter(apt -> apt.getCreatedAt().isBefore(lastMonth))
                    .count();

            if (lastMonthCount == 0) return 0;
            return ((double) (currentCount - lastMonthCount) / lastMonthCount) * 100;
        }

        return 0;
    }

    private int calculateNewThisMonth(String entity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        if (entity.equals("doctors")) {
            return (int) doctorRepository.findAll().stream()
                    .filter(d -> d.getUser().getCreatedAt().isAfter(startOfMonth))
                    .count();
        }

        return 0;
    }

    private AppointmentResponse convertToAppointmentResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        String specializationNames = appointment.getDoctor().getSpecializations()
                .stream()
                .map(Specialization::getName)
                .collect(Collectors.joining(", "));

        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatient().getId());
        response.setPatientName(appointment.getPatient().getUser().getFullName());
        response.setPatientEmail(appointment.getPatient().getUser().getEmail());
        response.setPatientPhone(appointment.getPatient().getUser().getPhone());
        response.setDoctorId(appointment.getDoctor().getId());
        response.setDoctorName(appointment.getDoctor().getUser().getFullName());
        response.setDoctorSpecialization(specializationNames);
        response.setDepartmentName(appointment.getDepartment() != null ?
                appointment.getDepartment().getName() : null);
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setAppointmentTime(appointment.getAppointmentTime());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setNotes(appointment.getNotes());
        response.setTokenNumber(appointment.getTokenNumber());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }

    public SuperAdminDashboardData getDashboardByAdminId(Long id) {
        try {
            long patientCount = patientRepository.count();
            long doctorCount = doctorRepository.count();
            long totalAppointment = appointmentRepository.count();
            long todayAppointment = appointmentRepository.countByAppointmentDate(LocalDate.now());

            return new SuperAdminDashboardData(patientCount, doctorCount, totalAppointment, todayAppointment);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AppointmentStats appointmentStatsByReceptionist(Long id) {
        try {
            long todayAppointment = appointmentRepository.countByAppointmentDate(LocalDate.now());
            long pendingCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.PENDING);
            long confirmedCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CONFIRMED);
            long completedCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.COMPLETED);
            long cancelledCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);

            return new AppointmentStats(todayAppointment, pendingCount, confirmedCount, completedCount, cancelledCount);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AppointmentStats appointmentStatsByPatient(Long id) {
        try {
            long todayAppointment = appointmentRepository.countByAppointmentDate(LocalDate.now());
            long completedCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.COMPLETED);
            long cancelledCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.CANCELLED);
            List<Appointment> appointments = appointmentRepository.findUpcomingAppointmentsByPatient(id, LocalDate.now());
            List<AppointmentResponse> appointmentResponse = appointments.stream()
                    .map(this::convertToAppointmentResponse)
                    .toList();
            AppointmentStats stats = new AppointmentStats();
            stats.setTodayTotal(todayAppointment);
            stats.setUpcoming(appointments.size());
            stats.setCompleted(completedCount);
            stats.setCancelled(cancelledCount);
            stats.setUpcomingAppointments(appointmentResponse);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public DoctorDashboardStats dashboardStatsByDoctor(Long id) {
        try {
            long todayAppointment = appointmentRepository.countByAppointmentDate(LocalDate.now());
            LocalDate today = LocalDate.now();
            LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
            LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);
            long thisWeekCount  = appointmentRepository.countByAppointmentDateBetween(startOfWeek, endOfWeek);
            long completedCount = appointmentRepository.countByStatus(Appointment.AppointmentStatus.COMPLETED);
            long pendingCount = appointmentRepository.countByStatusAndDoctorId(Appointment.AppointmentStatus.PENDING, id);
            long totalPatientsCount = appointmentRepository.countUniquePatientsByDoctorId(id);
            DoctorDashboardStats stats = new DoctorDashboardStats();
            Optional<Doctor> optional = doctorRepository.findById(id);
            if(optional.isPresent()) {
                Doctor doctor = optional.get();
                stats.setDepartment(doctor.getDepartment().getName());
                String specializationNames =
                doctor.getSpecializations()
                        .stream()
                        .map(Specialization::getName)
                        .collect(Collectors.joining(", "));
                stats.setSpecialization(specializationNames);
                stats.setExperienceYears(doctor.getExperienceYears());
            }
            stats.setTodayTotal(todayAppointment);
            stats.setThisWeekTotal(thisWeekCount);
            stats.setPending(pendingCount);
            stats.setTotalPatients(totalPatientsCount);
            return stats;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}