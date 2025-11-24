package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.service.DoctorAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("hospital/api/doctors/{doctorId}/availability")
public class DoctorAvailabilityController {

    @Autowired
    private DoctorAvailabilityService availabilityService;

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DOCTOR')")
    @PostMapping
    public ResponseEntity<DoctorAvailabilityResponse> createOrUpdateAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody DoctorAvailabilityRequest request) {

        DoctorAvailabilityResponse response = availabilityService.createOrUpdateAvailability(doctorId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DOCTOR')")
    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse> setBulkAvailability(
            @PathVariable Long doctorId,
            @Valid @RequestBody BulkAvailabilityRequest request) {

        ApiResponse response = availabilityService.setBulkAvailability(doctorId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<DoctorAvailabilityResponse>> getDoctorAvailability(
            @PathVariable Long doctorId) {

        List<DoctorAvailabilityResponse> response = availabilityService.getDoctorAvailability(doctorId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/day/{day}")
    public ResponseEntity<DoctorAvailabilityResponse> getAvailabilityByDay(
            @PathVariable Long doctorId,
            @PathVariable String day) {

        DoctorAvailabilityResponse response = availabilityService.getAvailabilityByDay(doctorId, day);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DOCTOR')")
    @DeleteMapping("/{availabilityId}")
    public ResponseEntity<ApiResponse> deleteAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId) {

        availabilityService.deleteAvailability(doctorId, availabilityId);

        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Availability deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'DOCTOR')")
    @PatchMapping("/{availabilityId}/toggle")
    public ResponseEntity<DoctorAvailabilityResponse> toggleAvailability(
            @PathVariable Long doctorId,
            @PathVariable Long availabilityId) {

        DoctorAvailabilityResponse response = availabilityService.toggleAvailability(doctorId, availabilityId);
        return ResponseEntity.ok(response);
    }
}