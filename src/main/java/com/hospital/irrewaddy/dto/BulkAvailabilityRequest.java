package com.hospital.irrewaddy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BulkAvailabilityRequest {

    @NotNull(message = "Availabilities list is required")
    @Size(min = 1, max = 7, message = "Must provide availability for at least 1 day, maximum 7 days")
    private List<DoctorAvailabilityRequest> availabilities;

    // Getters and Setters
    public List<DoctorAvailabilityRequest> getAvailabilities() {
        return availabilities;
    }

    public void setAvailabilities(List<DoctorAvailabilityRequest> availabilities) {
        this.availabilities = availabilities;
    }
}