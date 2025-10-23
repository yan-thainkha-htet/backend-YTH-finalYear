package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.CreateReceptionistRequest;
import com.hospital.irrewaddy.dto.ReceptionistResponse;
import com.hospital.irrewaddy.service.ReceptionistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hospital/api/receptionists")
@CrossOrigin(origins = "*")
public class ReceptionistController {

    @Autowired
    private ReceptionistService receptionistService;

    // Get all receptionists (Admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllReceptionists() {
        try {
            List<ReceptionistResponse> receptionists = receptionistService.getAllReceptionists();
            return ResponseEntity.ok(receptionists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get active receptionists
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getActiveReceptionists() {
        try {
            List<ReceptionistResponse> receptionists = receptionistService.getActiveReceptionists();
            return ResponseEntity.ok(receptionists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get on-duty receptionists
    @GetMapping("/on-duty")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> getOnDutyReceptionists() {
        try {
            List<ReceptionistResponse> receptionists = receptionistService.getOnDutyReceptionists();
            return ResponseEntity.ok(receptionists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get receptionists by shift
    @GetMapping("/shift/{shift}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getReceptionistsByShift(@PathVariable String shift) {
        try {
            List<ReceptionistResponse> receptionists = receptionistService.getReceptionistsByShift(shift);
            return ResponseEntity.ok(receptionists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get receptionist by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> getReceptionistById(@PathVariable Long id) {
        try {
            ReceptionistResponse receptionist = receptionistService.getReceptionistById(id);
            return ResponseEntity.ok(receptionist);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Create receptionist (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createReceptionist(@Valid @RequestBody CreateReceptionistRequest request) {
        try {
            ReceptionistResponse response = receptionistService.createReceptionist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update receptionist (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateReceptionist(@PathVariable Long id,
                                                @Valid @RequestBody CreateReceptionistRequest request) {
        try {
            ReceptionistResponse response = receptionistService.updateReceptionist(id, request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update duty status
    @PatchMapping("/{id}/duty-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<?> updateDutyStatus(@PathVariable Long id,
                                              @RequestBody Map<String, Boolean> request) {
        try {
            Boolean isOnDuty = request.get("isOnDuty");
            if (isOnDuty == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("isOnDuty field is required");
            }
            ReceptionistResponse response = receptionistService.updateDutyStatus(id, isOnDuty);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Delete receptionist (Admin only) - Soft delete
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReceptionist(@PathVariable Long id) {
        try {
            receptionistService.deleteReceptionist(id);
            return ResponseEntity.ok("Receptionist deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}