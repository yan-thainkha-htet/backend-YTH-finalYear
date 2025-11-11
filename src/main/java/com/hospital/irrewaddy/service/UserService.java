package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.EmailOTP;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.EmailOTPRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.security.CustomUserDetails;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class UserService {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailOTPRepository emailOTPRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Extract user ID from authentication object
     * Falls back to JWT token if authentication principal is not CustomUserDetails
     */
    public Long extractUserIdFromAuth(Authentication authentication, HttpServletRequest request) {
        // Method 1: Try to get from Authentication object (works when using CustomUserDetails)
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getId();
        }

        // Method 2: Fallback - Extract from JWT token directly
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt)) {
            try {
                Long userId = jwtUtil.extractUserId(jwt);
                if (userId != null) {
                    return userId;
                }
            } catch (Exception e) {
                // Token parsing failed
            }
        }

        throw new RuntimeException("Unable to extract user ID from authentication. Please ensure you're logged in with a valid token.");
    }

    /**
     * Extract JWT token from Authorization header
     */
    public String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

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
//        if (!user.getRole().equals(User.UserRole.ADMIN)) {
//            throw new RuntimeException("Unauthorized");
//        }

        // Check if email is verified
//        if (!user.getIsEmailVerified()) {
//            throw new RuntimeException("Email must be verified before updating password");
//        }

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
