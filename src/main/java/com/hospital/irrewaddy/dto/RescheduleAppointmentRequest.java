package com.hospital.irrewaddy.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public class RescheduleAppointmentRequest {

    @NotNull(message = "New appointment date is required")
    @FutureOrPresent(message = "Appointment date must be today or in the future")
    private LocalDate newAppointmentDate;

    @NotNull(message = "New appointment time is required")
    private LocalTime newAppointmentTime;

    private String reason;

    // Constructors
    public RescheduleAppointmentRequest() {
    }

    public RescheduleAppointmentRequest(LocalDate newAppointmentDate, LocalTime newAppointmentTime, String reason) {
        this.newAppointmentDate = newAppointmentDate;
        this.newAppointmentTime = newAppointmentTime;
        this.reason = reason;
    }

    // Getters and Setters
    public LocalDate getNewAppointmentDate() {
        return newAppointmentDate;
    }

    public void setNewAppointmentDate(LocalDate newAppointmentDate) {
        this.newAppointmentDate = newAppointmentDate;
    }

    public LocalTime getNewAppointmentTime() {
        return newAppointmentTime;
    }

    public void setNewAppointmentTime(LocalTime newAppointmentTime) {
        this.newAppointmentTime = newAppointmentTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}