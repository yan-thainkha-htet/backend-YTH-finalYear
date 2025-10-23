package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.*;
import com.hospital.irrewaddy.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AppointmentResponse createAppointment(CreateAppointmentRequest request, String username) {
        // Get patient by username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        // Validate doctor
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Check if doctor is active
        if (!doctor.getUser().getIsActive()) {
            throw new RuntimeException("Doctor is not available for appointments");
        }

        // Check if appointment slot is available
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                request.getDoctorId(), request.getAppointmentDate(), request.getAppointmentTime())) {
            throw new RuntimeException("This appointment slot is already booked");
        }

        // Create appointment
        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        // Set department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            appointment.setDepartment(department);
        } else if (doctor.getDepartment() != null) {
            appointment.setDepartment(doctor.getDepartment());
        }

        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setStatus(Appointment.AppointmentStatus.PENDING);

        // Generate token number
        Integer maxToken = appointmentRepository.getMaxTokenNumberForDoctorAndDate(
                request.getDoctorId(), request.getAppointmentDate());
        appointment.setTokenNumber(maxToken != null ? maxToken + 1 : 1);

        // Save appointment
        appointment = appointmentRepository.save(appointment);

        return convertToResponse(appointment, "Appointment booked successfully");
    }

    public List<AppointmentResponse> getMyAppointments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        List<Appointment> appointments = appointmentRepository
                .findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId());

        return appointments.stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getUpcomingAppointments(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient profile not found"));

        List<Appointment> appointments = appointmentRepository
                .findUpcomingAppointmentsByPatient(patient.getId(), LocalDate.now());

        return appointments.stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return convertToResponse(appointment, null);
    }

    public List<AppointmentResponse> getDoctorAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepository
                .findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(doctorId);

        return appointments.stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getDoctorTodayAppointments(Long doctorId) {
        List<Appointment> appointments = appointmentRepository
                .findTodayAppointmentsByDoctor(doctorId, LocalDate.now());

        return appointments.stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    public List<AppointmentResponse> getAppointmentsByStatus(Appointment.AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(apt -> convertToResponse(apt, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public AppointmentResponse updateAppointmentStatus(Long id, UpdateAppointmentStatusRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appointment.setStatus(request.getStatus());
        if (request.getNotes() != null && !request.getNotes().isEmpty()) {
            appointment.setNotes(request.getNotes());
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return convertToResponse(appointment, "Appointment status updated successfully");
    }

    @Transactional
    public AppointmentResponse rescheduleAppointment(Long id, RescheduleAppointmentRequest request, String username) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify ownership
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only reschedule your own appointments");
        }

        // Check if new slot is available
        if (appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                appointment.getDoctor().getId(), request.getNewAppointmentDate(), request.getNewAppointmentTime())) {
            throw new RuntimeException("This appointment slot is already booked");
        }

        appointment.setAppointmentDate(request.getNewAppointmentDate());
        appointment.setAppointmentTime(request.getNewAppointmentTime());
        appointment.setStatus(Appointment.AppointmentStatus.RESCHEDULED);
        if (request.getReason() != null) {
            appointment.setNotes(appointment.getNotes() + "\nRescheduled: " + request.getReason());
        }
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return convertToResponse(appointment, "Appointment rescheduled successfully");
    }

    @Transactional
    public AppointmentResponse cancelAppointment(Long id, String username) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        // Verify ownership
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only cancel your own appointments");
        }

        appointment.setStatus(Appointment.AppointmentStatus.CANCELLED);
        appointment.setUpdatedAt(LocalDateTime.now());

        appointment = appointmentRepository.save(appointment);

        return convertToResponse(appointment, "Appointment cancelled successfully");
    }

    @Transactional
    public void deleteAppointment(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        appointmentRepository.delete(appointment);
    }

    // Helper method to convert Appointment to Response
    private AppointmentResponse convertToResponse(Appointment appointment, String message) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientId(appointment.getPatient().getId());
        response.setPatientName(appointment.getPatient().getUser().getFullName());
        response.setPatientEmail(appointment.getPatient().getUser().getEmail());
        response.setPatientPhone(appointment.getPatient().getUser().getPhone());
        response.setDoctorId(appointment.getDoctor().getId());
        response.setDoctorName(appointment.getDoctor().getUser().getFullName());
        response.setDoctorSpecialization(appointment.getDoctor().getSpecialization());
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
        response.setMessage(message);
        return response;
    }
}