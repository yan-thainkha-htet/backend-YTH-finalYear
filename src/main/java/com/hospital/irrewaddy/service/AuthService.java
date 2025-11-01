package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.PatientRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private EmailService emailService;

    @Value("${app.otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Value("${app.otp.length:6}")
    private int otpLength;

//    @Transactional
//    public AuthResponse register(RegisterRequest request) {
//        // Validate password confirmation
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new RuntimeException("Passwords do not match");
//        }
//
//        // Generate username from email
//        String username = request.generateUsername();
//
//        // Check username uniqueness
//        if (userRepository.existsByUsername(username)) {
//            // If username exists, append a number
//            int counter = 1;
//            while (userRepository.existsByUsername(username + counter)) {
//                counter++;
//            }
//            username = username + counter;
//        }
//
//        // Check email uniqueness
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new RuntimeException("Email already exists");
//        }
//
//        // Check phone uniqueness
//        if (userRepository.existsByPhone(request.getPhone())) {
//            throw new RuntimeException("Phone number already exists");
//        }
//
//        // Validate email format
//        if (!ValidationUtil.isValidEmail(request.getEmail())) {
//            throw new RuntimeException("Invalid email format");
//        }
//
//        // Validate phone format
//        if (!ValidationUtil.isValidPhone(request.getPhone())) {
//            throw new RuntimeException("Invalid phone number format");
//        }
//
//        // Validate password strength
//        String passwordError = ValidationUtil.validatePassword(request.getPassword());
//        if (passwordError != null) {
//            throw new RuntimeException(passwordError);
//        }
//
//        // Create User entity
//        User user = new User();
//        user.setFullName(request.getFullName());
//        user.setUsername(username);
//        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
//        user.setEmail(request.getEmail().trim().toLowerCase());
//        user.setPhone(request.getPhone().trim());
//        user.setGender(request.getGender());
//        user.setRole(User.UserRole.PATIENT);
//        // Auto-assign PATIENT role
//        user.setIsActive(true);
//        user.setDateOfBirth(request.getDateOfBirth());
//        user.setAddress(request.getAddress());
//        user.setMustChangePassword(true);
//
//
//        // Save user first
//        user = userRepository.save(user);
//
//        // Create Patient entity with additional information
//        Patient patient = new Patient();
//        patient.setUser(user);
//
//
//
//        // Save patient
//        patientRepository.save(patient);
//
//        // Generate token
//        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
//
//        return new AuthResponse(
//                token,
//                user.getUsername(),
//                user.getEmail(),
//                user.getRole(),
//                user.getMustChangePassword(), // Add this field
//                "Registration successful"
//        );
//    }

    public AuthResponse login(LoginRequest request) {
        try {
            String usernameOrEmail = request.getUsernameOrEmail().trim().toLowerCase();

            // Find user by username or email
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> new RuntimeException("Invalid credentials"));

            // Check if user is active
            if (!user.getIsActive()) {
                throw new RuntimeException("Account is deactivated. Please contact support.");
            }

            // Authenticate user with username (not email)
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(), // Use actual username for authentication
                            request.getPassword()
                    )
            );

            // Generate token
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

            // Create response with must change password flag
            String message = "Login successful";
            if (user.getMustChangePassword() != null && user.getMustChangePassword()) {
                message = "Login successful. You must change your password before continuing.";
            }

            return new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getMustChangePassword(), // Add this field
                    message
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public String changePassword(String username, ChangePasswordRequest request) {
        // Find user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        // Validate new password
        String passwordError = ValidationUtil.validatePassword(request.getNewPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Check if new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        // Check if new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false); // Clear the flag
        userRepository.save(user);

        return "Password changed successfully";
    }


    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Check if user is active
        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated. Please contact support.");
        }

        // Check OTP attempt limit (max 5 attempts per hour)
        if (user.getOtpAttempts() != null && user.getOtpAttempts() >= 5) {
            if (user.getOtpExpiry() != null && user.getOtpExpiry().isAfter(LocalDateTime.now())) {
                long minutesLeft = java.time.Duration.between(LocalDateTime.now(), user.getOtpExpiry()).toMinutes();
                throw new RuntimeException("Too many OTP requests. Please try again after " + minutesLeft + " minutes.");
            } else {
                // Reset attempts after expiry
                user.setOtpAttempts(0);
            }
        }

        // Generate 6-digit OTP
        String otp = generateOtp(otpLength);

        // Save OTP and expiry
        user.setPasswordResetOtp(passwordEncoder.encode(otp)); // Store hashed OTP for security
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        user.setOtpAttempts(user.getOtpAttempts() != null ? user.getOtpAttempts() + 1 : 1);
        userRepository.save(user);

        // Send OTP via email
        emailService.sendPasswordResetOtp(user.getEmail(), user.getFullName(), otp, otpExpiryMinutes);

        return "OTP sent to your email. It will expire in " + otpExpiryMinutes + " minutes.";
    }

    public String verifyOtp(VerifyOTPRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

        // Check if OTP exists
        if (user.getPasswordResetOtp() == null) {
            throw new RuntimeException("No OTP found. Please request a new one.");
        }

        // Check if OTP is expired
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Verify OTP
        if (!passwordEncoder.matches(request.getOtpCode(), user.getPasswordResetOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        return "OTP verified successfully. You can now reset your password.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        // Find user by email
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new RuntimeException("Invalid email or OTP"));

        // Check if OTP exists
        if (user.getPasswordResetOtp() == null) {
            throw new RuntimeException("No OTP found. Please request a new one.");
        }

        // Check if OTP is expired
        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        // Verify OTP
        if (!passwordEncoder.matches(request.getOtp(), user.getPasswordResetOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Validate new password
        String passwordError = ValidationUtil.validatePassword(request.getNewPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Check if passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Check if new password is different from old password
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new RuntimeException("New password must be different from your current password");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setPasswordResetOtp(null); // Clear OTP
        user.setOtpExpiry(null);
        user.setOtpAttempts(0);
        user.setMustChangePassword(false); // Clear force change flag if it was set
        userRepository.save(user);

        // Send confirmation email
        emailService.sendPasswordResetConfirmation(user.getEmail(), user.getFullName());

        return "Password reset successfully. You can now login with your new password.";
    }

    @Transactional
    public String resendOtp(ForgotPasswordRequest request) {
        // Reuse forgotPassword logic
        return forgotPassword(request);
    }

    // Helper method to generate OTP
    private String generateOtp(int length) {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}