package com.hospital.irrewaddy.exception;

public class DoctorAvailabilityResourceNotFoundException extends RuntimeException {

    public DoctorAvailabilityResourceNotFoundException(String message) {
        super(message);
    }

    public DoctorAvailabilityResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}