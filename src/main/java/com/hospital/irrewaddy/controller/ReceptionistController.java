package com.hospital.irrewaddy.controller;

import com.hospital.irrewaddy.dto.CreateReceptionistRequest;
import com.hospital.irrewaddy.dto.CreateReceptionistResponse;
import com.hospital.irrewaddy.dto.ReceptionistResponse;
import com.hospital.irrewaddy.model.Receptionist;
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

    // Create receptionist (Super_Admin & Admin only)
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> createReceptionist(@Valid @RequestBody CreateReceptionistRequest request) {
        try {
            CreateReceptionistResponse response = receptionistService.createReceptionist(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get all receptionists (Admin only)
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<?> getAllReceptionists() {
        try {
            List<ReceptionistResponse> receptionists = receptionistService.getAllReceptionists();
            return ResponseEntity.ok(receptionists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // Get active receptionists
//    @GetMapping("/active")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//    public ResponseEntity<?> getActiveReceptionists() {
//        try {
//            List<ReceptionistResponse> receptionists = receptionistService.getActiveReceptionists();
//            return ResponseEntity.ok(receptionists);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

    // Get receptionists by shift
//    @GetMapping("/shift/{shift}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//    public ResponseEntity<?> getReceptionistsByShift(@PathVariable Receptionist.Shift shift) {
//        try {
//            List<ReceptionistResponse> receptionists = receptionistService.getReceptionistsByShift(shift);
//            return ResponseEntity.ok(receptionists);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }

    // Get receptionist by ID
//    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//    public ResponseEntity<?> getReceptionistById(@PathVariable Long id) {
//        try {
//            ReceptionistResponse receptionist = receptionistService.getReceptionistById(id);
//            return ResponseEntity.ok(receptionist);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
//        }
//    }



    // Update receptionist (Admin only)
//    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//    public ResponseEntity<?> updateReceptionist(@PathVariable Long id,
//                                                @Valid @RequestBody CreateReceptionistRequest request) {
//        try {
//            ReceptionistResponse response = receptionistService.updateReceptionist(id, request);
//            return ResponseEntity.ok(response);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
    // Delete receptionist (Admin only) - Soft delete
//    @DeleteMapping("/{id}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
//    public ResponseEntity<?> deleteReceptionist(@PathVariable Long id) {
//        try {
//            receptionistService.deleteReceptionist(id);
//            return ResponseEntity.ok("Receptionist deactivated successfully");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        }
//    }
}