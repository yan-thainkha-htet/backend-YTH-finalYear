package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.*;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.service.DoctorService;
import com.hospital.irrewaddy.service.ReceptionistService;
import com.hospital.irrewaddy.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hospital/api/doctor/setup")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DoctorSetupController {

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @PostMapping("/complete-profile")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse> completeProfile(
            @Valid @RequestBody DoctorCompleteProfileRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = userService.extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = doctorService.completeProfile(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/resend-otp")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse> resendOTP(
            @RequestParam String email) {

        ApiResponse response = userService.sendEmailOTP(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-otp")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse> verifyOTP(
            @Valid @RequestBody VerifyOTPRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = userService.extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = userService.verifyEmailOTP(userId, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @PostMapping("/update-password")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse> updatePassword(
            @Valid @RequestBody UpdatePasswordRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest) {

        try {
            Long userId = userService.extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = userService.updatePassword(userId, request);
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
            Long userId = userService.extractUserIdFromAuth(authentication, httpRequest);
            ApiResponse response = userService.checkSetupStatus(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse errorResponse = new ApiResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }


}

