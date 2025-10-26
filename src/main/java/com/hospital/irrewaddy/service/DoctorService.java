package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.CreateDoctorRequest;
import com.hospital.irrewaddy.dto.DoctorResponse;
import com.hospital.irrewaddy.model.Department;
import com.hospital.irrewaddy.model.Doctor;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.DepartmentRepository;
import com.hospital.irrewaddy.repository.DoctorRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public DoctorResponse createDoctor(CreateDoctorRequest request) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        // Validate phone format
        if (!ValidationUtil.isValidPhone(request.getPhone())) {
            throw new RuntimeException("Invalid phone number format");
        }

        // Validate password strength
        String passwordError = ValidationUtil.validatePassword(request.getPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setFullName(request.getFullName().trim());
        user.setRole(User.UserRole.DOCTOR);
        user.setIsActive(request.getIsActive());
        user.setMustChangePassword(true);

        // Save user first
        user = userRepository.save(user);

        // Create Doctor entity
        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());
        doctor.setTotalPatients(0);


        // Set department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            doctor.setDepartment(department);
        }

        // Save doctor
        doctor = doctorRepository.save(doctor);

        try {
            String departmentName = doctor.getDepartment() != null ?
                    doctor.getDepartment().getName() : "Not assigned";

            emailService.sendDoctorWelcomeEmail(
                    user.getEmail(),
                    user.getFullName(),
                    user.getUsername(),
                    request.getPassword(), // Temporary password
                    doctor.getSpecialization(),
                    departmentName
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to doctor: " + e.getMessage());
            // Continue anyway - doctor was created successfully
        }

        // Convert to response
        return convertToResponse(doctor, "Doctor created successfully");
    }

    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(doctor -> convertToResponse(doctor, null))
                .collect(Collectors.toList());
    }

    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));
        return convertToResponse(doctor, null);
    }

    @Transactional
    public DoctorResponse updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        User user = doctor.getUser();

        // Update user information
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (!user.getPhone().equals(request.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Phone number already exists");
            }
            user.setPhone(request.getPhone().trim());
        }

        user.setFullName(request.getFullName().trim());
        user.setIsActive(request.getIsActive());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String passwordError = ValidationUtil.validatePassword(request.getPassword());
            if (passwordError != null) {
                throw new RuntimeException(passwordError);
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        // Update doctor information
        doctor.setSpecialization(request.getSpecialization());
        doctor.setQualification(request.getQualification());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());

        // Update department if provided
        if (request.getDepartmentId() != null) {
            Department department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found"));
            doctor.setDepartment(department);
        }

        doctor = doctorRepository.save(doctor);

        return convertToResponse(doctor, "Doctor updated successfully");
    }

    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Soft delete by deactivating user account
        User user = doctor.getUser();
        user.setIsActive(false);
        userRepository.save(user);
    }

    // Helper method to convert Doctor to Response
    private DoctorResponse convertToResponse(Doctor doctor, String message) {
        DoctorResponse response = new DoctorResponse();
        response.setId(doctor.getId());
        response.setUserId(doctor.getUser().getId());
        response.setUsername(doctor.getUser().getUsername());
        response.setFullName(doctor.getUser().getFullName());
        response.setEmail(doctor.getUser().getEmail());
        response.setPhone(doctor.getUser().getPhone());
        response.setSpecialization(doctor.getSpecialization());
        response.setQualification(doctor.getQualification());
        response.setExperienceYears(doctor.getExperienceYears());
        response.setBio(doctor.getBio());
        response.setTotalPatients(doctor.getTotalPatients());
        response.setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null);
        response.setIsActive(doctor.getUser().getIsActive());
        response.setMessage(message);
        return response;
    }
}