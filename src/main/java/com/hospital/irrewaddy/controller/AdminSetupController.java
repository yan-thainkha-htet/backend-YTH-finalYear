package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.security.CustomUserDetails;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospital/api/admin/setup")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminSetupController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/complete-profile")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> completeProfile(
            @Valid @RequestBody AdminCompleteProfileRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        System.out.println("=== DEBUG ===");
        System.out.println("User: " + authentication.getName());
        System.out.println("Authorities: " + authentication.getAuthorities());
        System.out.println("Principal: " + authentication.getPrincipal());

        try {
            Long userId = extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = adminService.completeProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Step 2: Resend OTP
     */
    @PostMapping("/resend-otp")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> resendOTP(
            @RequestParam String email) {

        ApiResponse response = adminService.sendEmailOTP(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Step 3: Verify OTP
     */
    @PostMapping("/verify-otp")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> verifyOTP(
            @Valid @RequestBody VerifyOTPRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = adminService.verifyEmailOTP(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Step 4: Update password
     */
    @PostMapping("/update-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = adminService.updatePassword(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Check setup status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse> getSetupStatus(
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = adminService.checkSetupStatus(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    /**
     * Extract user ID from authentication object
     * Falls back to JWT token if authentication principal is not CustomUserDetails
     */
    private Long extractUserIdFromAuth(Authentication authentication, HttpServletRequest request) {
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
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}