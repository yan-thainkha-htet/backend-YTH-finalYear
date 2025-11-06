package com.hospital.irrewaddy.dto;

import java.util.Map;

public class DashboardStatsResponse {

    // User counts
    private Long totalPatients;
    private Long totalDoctors;
    private Long totalReceptionists;
    private Long totalAdmins;
    private Long totalSuperAdmins;
    private Long totalDepartments;

    // Appointment counts
    private Long totalAppointments;
    private Integer todayAppointments;
    private Integer todayCompletedAppointments;

    // Growth metrics
    private Double patientsGrowthPercent;
    private Double appointmentsGrowthPercent;
    private Integer doctorsAddedThisMonth;

    // Appointment status breakdown
    private Map<String, Integer> appointmentStatusBreakdown;

    // Constructors
    public DashboardStatsResponse() {
    }

    // Getters and Setters
    public Long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(Long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public Long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(Long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public Long getTotalReceptionists() {
        return totalReceptionists;
    }

    public void setTotalReceptionists(Long totalReceptionists) {
        this.totalReceptionists = totalReceptionists;
    }

    public Long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(Long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public Long getTotalSuperAdmins() {
        return totalSuperAdmins;
    }

    public void setTotalSuperAdmins(Long totalSuperAdmins) {
        this.totalSuperAdmins = totalSuperAdmins;
    }

    public Long getTotalDepartments() {
        return totalDepartments;
    }

    public void setTotalDepartments(Long totalDepartments) {
        this.totalDepartments = totalDepartments;
    }

    public Long getTotalAppointments() {
        return totalAppointments;
    }

    public void setTotalAppointments(Long totalAppointments) {
        this.totalAppointments = totalAppointments;
    }

    public Integer getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(Integer todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public Integer getTodayCompletedAppointments() {
        return todayCompletedAppointments;
    }

    public void setTodayCompletedAppointments(Integer todayCompletedAppointments) {
        this.todayCompletedAppointments = todayCompletedAppointments;
    }

    public Double getPatientsGrowthPercent() {
        return patientsGrowthPercent;
    }

    public void setPatientsGrowthPercent(Double patientsGrowthPercent) {
        this.patientsGrowthPercent = patientsGrowthPercent;
    }

    public Double getAppointmentsGrowthPercent() {
        return appointmentsGrowthPercent;
    }

    public void setAppointmentsGrowthPercent(Double appointmentsGrowthPercent) {
        this.appointmentsGrowthPercent = appointmentsGrowthPercent;
    }

    public Integer getDoctorsAddedThisMonth() {
        return doctorsAddedThisMonth;
    }

    public void setDoctorsAddedThisMonth(Integer doctorsAddedThisMonth) {
        this.doctorsAddedThisMonth = doctorsAddedThisMonth;
    }

    public Map<String, Integer> getAppointmentStatusBreakdown() {
        return appointmentStatusBreakdown;
    }

    public void setAppointmentStatusBreakdown(Map<String, Integer> appointmentStatusBreakdown) {
        this.appointmentStatusBreakdown = appointmentStatusBreakdown;
    }
}