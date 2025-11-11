package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.Admin;
import com.hospital.irrewaddy.model.EmailOTP;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.AdminRepository;
import com.hospital.irrewaddy.repository.EmailOTPRepository;
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
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;


    @Transactional
    public CreateAdminResponse createAdmin(CreateAdminRequest request) {
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


        user.setRole(User.UserRole.ADMIN);
        user.setIsActive(request.getIsActive());
        user.setMustChangePassword(true);
        user = userRepository.save(user);

        Admin admin = new Admin();
        admin.setUser(user);
        admin = adminRepository.save(admin);

        try {
            emailService.sendAdminWelcomeEmail(
                    user.getEmail(),
                    //user.getFullName(),
                    user.getUsername(),
                    request.getPassword() // Temporary password
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to admin: " + e.getMessage());
            // Continue anyway - admin was created successfully
        }

        return convertToCreateAdminResponse(user, "Admin created successfully");
    }

    /**
     * Step 1: Complete profile information
     */
    @Transactional
    public ApiResponse completeProfile(Long userId, AdminCompleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is super admin
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("Only admin can complete this setup");
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

        // Update user information
        user.setFullName(request.getFullName().trim());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());

        userRepository.save(user);

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


    public List<AdminResponse> getAllAdmins() {
        List<User> admins = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .collect(Collectors.toList());

        return admins.stream()
                .map(admin -> convertToResponse(admin, null))
                .collect(Collectors.toList());
    }

    public AdminResponse getAdminById(Long id) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        return convertToResponse(admin, null);
    }

    @Transactional
    public AdminResponse updateAdmin(Long id, CreateAdminRequest request) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Update email if changed
        if (!admin.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            admin.setEmail(request.getEmail().trim().toLowerCase());
        }

        // Update phone if changed
//        if (!admin.getPhone().equals(request.getPhone())) {
//            if (userRepository.existsByPhone(request.getPhone())) {
//                throw new RuntimeException("Phone number already exists");
//            }
//            admin.setPhone(request.getPhone().trim());
//        }

        // Update username if changed
        if (!admin.getUsername().equals(request.getUsername().toLowerCase())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            admin.setUsername(request.getUsername().trim().toLowerCase());
        }

        //admin.setFullName(request.getFullName().trim());
        admin.setIsActive(request.getIsActive());

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String passwordError = ValidationUtil.validatePassword(request.getPassword());
            if (passwordError != null) {
                throw new RuntimeException(passwordError);
            }
            admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        admin = userRepository.save(admin);

        return convertToResponse(admin, "Admin updated successfully");
    }

    @Transactional
    public void deleteAdmin(Long id) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Check if this is the last admin
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN && user.getIsActive())
                .count();

        if (adminCount <= 1) {
            throw new RuntimeException("Cannot delete the last active admin");
        }

        // Soft delete by deactivating
        admin.setIsActive(false);
        userRepository.save(admin);
    }

    @Transactional
    public void hardDeleteAdmin(Long id) {
        User admin = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (admin.getRole() != User.UserRole.ADMIN) {
            throw new RuntimeException("User is not an admin");
        }

        // Check if this is the last admin
        long adminCount = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.ADMIN)
                .count();

        if (adminCount <= 1) {
            throw new RuntimeException("Cannot delete the last admin");
        }

        userRepository.delete(admin);
    }

    // Helper method to convert User to AdminResponse
    private AdminResponse convertToResponse(User admin, String message) {
        AdminResponse response = new AdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setPhone(admin.getPhone());
        response.setGender(admin.getGender());
        response.setFullName(admin.getFullName());
        response.setRole(admin.getRole().toString());
        response.setIsActive(admin.getIsActive());
        response.setCreatedAt(admin.getCreatedAt());
        response.setMessage(message);
        return response;
    }

    private CreateAdminResponse convertToCreateAdminResponse(User admin, String message) {
        CreateAdminResponse response = new CreateAdminResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setEmail(admin.getEmail());
        response.setRole(admin.getRole().toString());

        return response;
    }
}