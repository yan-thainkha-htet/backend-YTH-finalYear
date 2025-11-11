package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.*;
import com.hospital.irrewaddy.repository.AdminRepository;
import com.hospital.irrewaddy.repository.EmailOTPRepository;
import com.hospital.irrewaddy.repository.PatientRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.security.CustomUserDetails;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;


    @Transactional
    public CreatePatientResponse createPatient(CreatePatientRequest request) {
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

        // Create admin user
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());


        user.setRole(User.UserRole.PATIENT);
        user.setIsActive(request.getIsActive());
        user.setMustChangePassword(true);
        user = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(user);
        patient = patientRepository.save(patient);

//        try {
//            emailService.sendAdminWelcomeEmail(
//                    user.getEmail(),
//                    //user.getFullName(),
//                    user.getUsername(),
//                    request.getPassword() // Temporary password
//            );
//        } catch (Exception e) {
//            System.err.println("Failed to send welcome email to admin: " + e.getMessage());
//            // Continue anyway - admin was created successfully
//        }

        return convertToCreatePatientResponse(user, "Patient created successfully");
    }

    @Transactional
    public ApiResponse completeProfile(Long userId, PatientCompleteProfileRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Patient patient = patientRepository.findByUserId(userId).
                orElseThrow(() -> new RuntimeException("Patient not found"));

        // Verify user is receptionist
        if (!user.getRole().equals(User.UserRole.PATIENT)){
            throw new RuntimeException("Only Patient can complete this setup");
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

        // Update user fields
        user.setFullName(request.getFullName().trim());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setAddress(request.getAddress().trim());
        user.setIsProfileCompleted(true);

        patient.setBloodGroup(request.getBloodGroup());

        userRepository.save(user);
        patientRepository.save(patient);

        CustomUserDetails updatedUserDetails = new CustomUserDetails(user);
        String newToken = jwtUtil.generateToken(updatedUserDetails);

        // Return response with new token
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Profile completed successfully.");

        Map<String, Object> data = new HashMap<>();
        data.put("newToken", newToken);  // ⭐ Send new token to client
        data.put("email", user.getEmail());
        response.setData(data);

        return response;
    }

    private CreatePatientResponse convertToCreatePatientResponse(User patient, String message) {
        CreatePatientResponse response = new CreatePatientResponse();
        response.setId(patient.getId());
        response.setUsername(patient.getUsername());
        response.setEmail(patient.getEmail());
        response.setRole(patient.getRole().toString());
        response.setMessage(message);

        return response;
    }
}