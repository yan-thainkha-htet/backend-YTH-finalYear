package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.AdminResponse;
import com.hospital.irrewaddy.dto.CreateAdminRequest;
import com.hospital.irrewaddy.model.Admin;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.AdminRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public AdminResponse createAdmin(CreateAdminRequest request) {
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

        // Create admin user
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setGender(User.Gender.valueOf(request.getGender()));
        user.setFullName(request.getFullName().trim());
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
                    user.getFullName(),
                    user.getUsername(),
                    request.getPassword() // Temporary password
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to admin: " + e.getMessage());
            // Continue anyway - admin was created successfully
        }

        return convertToResponse(user, "Admin created successfully");
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
        if (!admin.getPhone().equals(request.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Phone number already exists");
            }
            admin.setPhone(request.getPhone().trim());
        }

        // Update username if changed
        if (!admin.getUsername().equals(request.getUsername().toLowerCase())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new RuntimeException("Username already exists");
            }
            admin.setUsername(request.getUsername().trim().toLowerCase());
        }

        admin.setFullName(request.getFullName().trim());
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
}