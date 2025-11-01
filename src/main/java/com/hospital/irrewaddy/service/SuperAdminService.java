package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.EmailOTP;
import com.hospital.irrewaddy.model.User;
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
import java.util.Map;
import java.util.Random;

@Service
public class SuperAdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailOTPRepository emailOTPRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Step 1: Complete profile information
     */
    @Transactional
    public ApiResponse completeProfile(Long userId, CompleteProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify user is super admin
        if (!user.getRole().equals(User.UserRole.SUPER_ADMIN)) {
            throw new RuntimeException("Only super admin can complete this setup");
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

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
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

        // Update user information
        user.setFullName(request.getFullName().trim());
        user.setGender(User.Gender.valueOf(request.getGender().toUpperCase()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());

        userRepository.save(user);

        // Send OTP to email
        sendEmailOTP(request.getEmail());

        CustomUserDetails updatedUserDetails = new CustomUserDetails(user);
        String newToken = jwtUtil.generateToken(updatedUserDetails);

        // Return response with new token
        ApiResponse response = new ApiResponse();
        response.setSuccess(true);
        response.setMessage("Profile completed successfully. OTP sent to your email.");

        Map<String, Object> data = new HashMap<>();
        data.put("newToken", newToken);  // ⭐ Send new token to client
        data.put("email", user.getEmail());
        response.setData(data);

        return response;
    }

    /**
     * Step 2: Send OTP to email
     */
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

        // Verify user is super admin
        if (!user.getRole().equals(User.UserRole.SUPER_ADMIN)) {
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
}