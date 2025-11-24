package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.Doctor;
import com.hospital.irrewaddy.model.DoctorAvailability;
import com.hospital.irrewaddy.model.Specialization;
import com.hospital.irrewaddy.repository.DoctorAvailabilityRepository;
import com.hospital.irrewaddy.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorAvailabilityService {

    @Autowired
    private DoctorAvailabilityRepository availabilityRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Transactional
    public DoctorAvailabilityResponse createOrUpdateAvailability(Long doctorId, DoctorAvailabilityRequest request) {
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        // Validate day of week
        DoctorAvailability.DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DoctorAvailability.DayOfWeek.valueOf(request.getDayOfWeek().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid day of week: " + request.getDayOfWeek());
        }

        // Validate time range
        if (request.getStartTime().isAfter(request.getEndTime()) ||
                request.getStartTime().equals(request.getEndTime())) {
            throw new RuntimeException("Start time must be before end time");
        }

        // Check if availability already exists for this day
        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                .orElse(new DoctorAvailability());

        // Set or update values
        availability.setDoctor(doctor);
        availability.setDayOfWeek(dayOfWeek);
        availability.setStartTime(request.getStartTime());
        availability.setEndTime(request.getEndTime());
        availability.setAvailable(request.getIsAvailable());

        availability = availabilityRepository.save(availability);

        return convertToResponse(availability,
                availability.getId() == null ? "Availability created successfully" : "Availability updated successfully");
    }

    @Transactional
    public ApiResponse setBulkAvailability(Long doctorId, BulkAvailabilityRequest request) {
        // Validate doctor exists
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        List<DoctorAvailability> savedAvailabilities = new ArrayList<>();

        for (DoctorAvailabilityRequest availRequest : request.getAvailabilities()) {
            // Validate day of week
            DoctorAvailability.DayOfWeek dayOfWeek;
            try {
                dayOfWeek = DoctorAvailability.DayOfWeek.valueOf(availRequest.getDayOfWeek().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid day of week: " + availRequest.getDayOfWeek());
            }

            // Validate time range
            if (availRequest.getStartTime().isAfter(availRequest.getEndTime()) ||
                    availRequest.getStartTime().equals(availRequest.getEndTime())) {
                throw new RuntimeException("Start time must be before end time for " + availRequest.getDayOfWeek());
            }

            // Check if availability already exists for this day
            DoctorAvailability availability = availabilityRepository
                    .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                    .orElse(new DoctorAvailability());

            availability.setDoctor(doctor);
            availability.setDayOfWeek(dayOfWeek);
            availability.setStartTime(availRequest.getStartTime());
            availability.setEndTime(availRequest.getEndTime());
            availability.setAvailable(availRequest.getIsAvailable());

            savedAvailabilities.add(availabilityRepository.save(availability));
        }

        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Bulk availability updated successfully");

        Map<String, Object> data = new HashMap<>();
        data.put("count", savedAvailabilities.size());
        data.put("availabilities", savedAvailabilities.stream()
                .map(a -> convertToResponse(a, null))
                .collect(Collectors.toList()));
        response.setData(data);

        return response;
    }

    public List<DoctorAvailabilityResponse> getDoctorAvailability(Long doctorId) {
        // Validate doctor exists
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        List<DoctorAvailability> availabilities = availabilityRepository.findByDoctorId(doctorId);

        return availabilities.stream()
                .map(a -> convertToResponse(a, null))
                .collect(Collectors.toList());
    }

    public List<DoctorWithAvailabilityResponse> getAllDoctorsWithAvailability() {

        List<Doctor> doctors = doctorRepository.findAll();

        return doctors.stream()
                // Filter: doctor must have at least one available slot
                .filter(doctor ->
                        availabilityRepository.findByDoctorId(doctor.getId())
                                .stream()
                                .anyMatch(DoctorAvailability::getAvailable)   // only true availability
                )

                // Map doctor → response
                .map(doctor -> {

                    // Convert only available schedules
                    List<DoctorAvailabilityResponse> availabilityResponses =
                            availabilityRepository.findByDoctorId(doctor.getId())
                                    .stream()
                                    .filter(DoctorAvailability::getAvailable) // keep only available
                                    .map(a -> convertToResponse(a, null))
                                    .toList();

                    String specializationNames = doctor.getSpecializations()
                            .stream()
                            .map(Specialization::getName)
                            .collect(Collectors.joining(", "));

                    return new DoctorWithAvailabilityResponse(
                            doctor.getId(),
                            doctor.getUser().getFullName(),
                            doctor.getUser().getEmail(),
                            specializationNames,
                            doctor.getDepartment().getName(),
                            doctor.getQualification(),
                            doctor.getExperienceYears(),
                            availabilityResponses
                    );
                })
                .toList();
    }

    public DoctorAvailabilityResponse getAvailabilityByDay(Long doctorId, String day) {
        // Validate doctor exists
        doctorRepository.findById(doctorId)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + doctorId));

        // Validate day of week
        DoctorAvailability.DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DoctorAvailability.DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid day of week: " + day);
        }

        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, dayOfWeek)
                .orElseThrow(() -> new RuntimeException("No availability found for " + day));

        return convertToResponse(availability, null);
    }

    @Transactional
    public void deleteAvailability(Long doctorId, Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Availability does not belong to this doctor");
        }

        availabilityRepository.delete(availability);
    }

    @Transactional
    public DoctorAvailabilityResponse toggleAvailability(Long doctorId, Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new RuntimeException("Availability not found"));

        if (!availability.getDoctor().getId().equals(doctorId)) {
            throw new RuntimeException("Availability does not belong to this doctor");
        }

        availability.setAvailable(!availability.getAvailable());
        availability = availabilityRepository.save(availability);

        return convertToResponse(availability, "Availability status toggled successfully");
    }

    // Helper method to convert entity to response
    public DoctorAvailabilityResponse convertToResponse(DoctorAvailability availability, String message) {
        DoctorAvailabilityResponse response = new DoctorAvailabilityResponse();
        response.setId(availability.getId());
        response.setDoctorId(availability.getDoctor().getId());
        response.setDoctorName(availability.getDoctor().getUser().getFullName());
        response.setDayOfWeek(availability.getDayOfWeek().toString());
        response.setStartTime(availability.getStartTime());
        response.setEndTime(availability.getEndTime());
        response.setIsAvailable(availability.getAvailable());
        response.setMessage(message);
        return response;
    }
}