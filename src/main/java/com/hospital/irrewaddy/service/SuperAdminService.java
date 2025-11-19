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
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public ApiResponse completeProfile(Long userId, SuperAdminCompleteProfileRequest request) {
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
        userService.sendEmailOTP(request.getEmail());

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
}