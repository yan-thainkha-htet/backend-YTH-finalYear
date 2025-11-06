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
    private EmailOTPRepository emailOTPRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @Transactional
    public CreateAdminResponse createAdmin(CreateAdminRequest request) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
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

    /**
     * Step 2: Send OTP to email
     */
    @Transactional
    public ApiResponse sendEmailOTP(String email) {
        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Delete any existing OTPs for this email
        emailOTPRepository.deleteByEmail(email);

        // Create new OTP
        EmailOTP emailOTP = new EmailOTP();
        emailOTP.setEmail(email);
        emailOTP.setOtpCode(otpCode);
        emailOTP.setExpiresAt(LocalDateTime.now().plusMinutes(10)); // Expires in 10 minutes
        emailOTP.setIsUsed(false);

        emailOTPRepository.save(emailOTP);

        // Send OTP via email
        try {
            emailService.sendOTPEmail(email, otpCode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }

        return new ApiResponse("OTP sent to " + email, true);
    }

    /**
     * Step 3: Verify OTP
     */
    @Transactional
    public ApiResponse verifyEmailOTP(Long userId, VerifyOTPRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user email matches
        if (!user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new RuntimeException("Email does not match user account");
        }

        // Find latest OTP for this email
        EmailOTP emailOTP = emailOTPRepository
                .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No OTP found for this email"));

        // Check if OTP is expired
        if (emailOTP.isExpired()) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Verify OTP code
        if (!emailOTP.getOtpCode().equals(request.getOtpCode())) {
            throw new RuntimeException("Invalid OTP code");
        }

        // Mark OTP as used
        emailOTP.setIsUsed(true);
        emailOTPRepository.save(emailOTP);

        // Mark email as verified
        user.setIsEmailVerified(true);
        userRepository.save(user);

        return new ApiResponse("Email verified successfully. Please proceed to update your password.", true);
    }

    /**
     * Step 4: Update password
     */
    @Transactional
    public ApiResponse updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is admin
        if (!user.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("Unauthorized");
        }

        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            throw new RuntimeException("Email must be verified before updating password");
        }

        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Validate password strength
        String passwordError = ValidationUtil.validatePassword(request.getNewPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setIsProfileCompleted(true);

        userRepository.save(user);

        return new ApiResponse("Password updated successfully. Setup complete!", true);
    }

    /**
     * Check if super admin setup is complete
     */
    public ApiResponse checkSetupStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SetupStatusResponse response = new SetupStatusResponse();
        response.setProfileCompleted(user.getIsProfileCompleted() != null && user.getIsProfileCompleted());
        response.setEmailVerified(user.getIsEmailVerified() != null && user.getIsEmailVerified());
        response.setPasswordChanged(!user.getMustChangePassword());
        response.setSetupComplete(
                response.isProfileCompleted() &&
                        response.isEmailVerified() &&
                        response.isPasswordChanged()
        );

        return new ApiResponse("Setup status retrieved", true, response);
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