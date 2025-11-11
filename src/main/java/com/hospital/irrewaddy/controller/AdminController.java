package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.CreateAdminRequest;
import com.hospital.irrewaddy.dto.CreateAdminResponse;
import com.hospital.irrewaddy.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/hospital/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest request) {
        try {
            //log.info("Received request to create admin: {}", request.getUsername());
            CreateAdminResponse response = adminService.createAdmin(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            //log.error("Error creating admin: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }
}
