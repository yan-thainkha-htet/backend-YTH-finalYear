package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.*;
import com.hospital.irrewaddy.repository.DepartmentRepository;
import com.hospital.irrewaddy.repository.DoctorRepository;
import com.hospital.irrewaddy.repository.SpecializationRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.security.CustomUserDetails;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Transactional
    public CreateDoctorResponse createDoctor(CreateDoctorRequest request) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate email format
        if (!ValidationUtil.isValidEmail(request.getEmail())) {
            throw new RuntimeException("Invalid email format");
        }

        // Validate password strength
        String passwordError = ValidationUtil.validatePassword(request.getPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Validate and fetch department
        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + request.getDepartmentId()));


        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(User.UserRole.DOCTOR);
        user.setIsActive(request.getIsActive());
        user.setMustChangePassword(true);

        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor = doctorRepository.save(doctor);

        try {
            emailService.sendDoctorWelcomeEmail(
                    user.getEmail(),
                    user.getUsername(),
                    request.getPassword() // Temporary password

            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to receptionist: " + e.getMessage());
            // Continue anyway - receptionist was created successfully
        }

        return convertToCreateDoctorResponse(user, doctor, "Doctor created successfully");
    }

    @Transactional
    public ApiResponse completeProfile(Long userId, DoctorCompleteProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        // Verify user is doctor
        if (!user.getRole().equals(User.UserRole.DOCTOR)){
            throw new RuntimeException("Only doctor can complete this setup");
        }

        // Check if profile already completed
        if (user.getIsProfileCompleted()) {
            throw new RuntimeException("Profile already completed");
        }

        // Check username uniqueness (if different from current)
        if (!user.getUsername().equals(request.getUsername().toLowerCase())) {
            if (userRepository.existsByUsername(request.getUsername().toLowerCase())) {
                throw new RuntimeException("Username already exists");
            }
        }

        // Check phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already exists");
        }

        // Validate phone format
        if (!ValidationUtil.isValidPhone(request.getPhone())) {
            throw new RuntimeException("Invalid phone number format");
        }

        // Validate date of birth
        if (request.getDateOfBirth() == null) {
            throw new RuntimeException("Date of birth is required");
        }

        // Validate and fetch all specializations
        Set<Specialization> specializations = new HashSet<>();
        for (Long specId : request.getSpecializationIds()) {
            Specialization specialization = specializationRepository.findById(specId)
                    .orElseThrow(() -> new RuntimeException("Specialization not found with id: " + specId));
            specializations.add(specialization);
        }

        // Update user fields
        user.setFullName(request.getFullName().trim());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress().trim());
        user.setIsProfileCompleted(true);

        // Update doctor fields
        doctor.setQualification(request.getQualification().trim());
        doctor.setExperienceYears(request.getYearOfExperience());
        doctor.setBio(request.getBio().trim());

        // Clear existing specializations and add new ones
        doctor.clearSpecializations();
        for (Specialization specialization : specializations) {
            doctor.addSpecialization(specialization);
        }

        userRepository.save(user);
        doctorRepository.save(doctor);

        CustomUserDetails updatedUserDetails = new CustomUserDetails(user);
        String newToken = jwtUtil.generateToken(updatedUserDetails);

        // Return response with new token
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Profile completed successfully.");

        Map<String, Object> data = new HashMap<>();
        data.put("newToken", newToken);
        data.put("email", user.getEmail());
        response.setData(data);

        return response;
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

    public DoctorResponse getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
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

//        if (!user.getPhone().equals(request.getPhone())) {
//            if (userRepository.existsByPhone(request.getPhone())) {
//                throw new RuntimeException("Phone number already exists");
//            }
//            user.setPhone(request.getPhone().trim());
//        }
//
//        user.setFullName(request.getFullName().trim());
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
//        doctor.setSpecialization(request.getSpecialization());
//        doctor.setQualification(request.getQualification());
//        doctor.setExperienceYears(request.getExperienceYears());
//        doctor.setBio(request.getBio());

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

    private CreateDoctorResponse convertToCreateDoctorResponse(User user, Doctor doctor, String message) {
        CreateDoctorResponse response = new CreateDoctorResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        response.setDepartmentName(doctor.getDepartment().getName());
        response.setMessage("message");

        return response;
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
        response.setQualification(doctor.getQualification());
        response.setExperienceYears(doctor.getExperienceYears());
        response.setBio(doctor.getBio());
        response.setDepartmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null);
        response.setIsActive(doctor.getUser().getIsActive());
        response.setMessage(message);
        return response;
    }
}