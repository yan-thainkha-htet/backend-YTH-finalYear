package com.hospital.irrewaddy.dto;

import com.hospital.irrewaddy.model.Appointment;
import jakarta.validation.constraints.NotNull;

public class UpdateAppointmentStatusRequest {

    @NotNull(message = "Status is required")
    private Appointment.AppointmentStatus status;

    private String notes;

    // Constructors
    public UpdateAppointmentStatusRequest() {
    }

    public UpdateAppointmentStatusRequest(Appointment.AppointmentStatus status, String notes) {
        this.status = status;
        this.notes = notes;
    }

    // Getters and Setters
    public Appointment.AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(Appointment.AppointmentStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}