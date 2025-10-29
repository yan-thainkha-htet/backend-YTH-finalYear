package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.CreateReceptionistRequest;
import com.hospital.irrewaddy.dto.ReceptionistResponse;
import com.hospital.irrewaddy.model.Receptionist;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.ReceptionistRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReceptionistService {

    @Autowired
    private ReceptionistRepository receptionistRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ReceptionistResponse createReceptionist(CreateReceptionistRequest request) {
        // Check username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
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

        // Validate password strength
        String passwordError = ValidationUtil.validatePassword(request.getPassword());
        if (passwordError != null) {
            throw new RuntimeException(passwordError);
        }

        // Create User entity
        User user = new User();
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setGender( User.Gender.valueOf(request.getGender()) );
        user.setFullName(request.getFullName().trim());
        user.setRole(User.UserRole.RECEPTIONIST);
        user.setIsActive(request.getIsActive());
        user.setMustChangePassword(true);

        // Save user first
        user = userRepository.save(user);

        // Create Receptionist entity
        Receptionist receptionist = new Receptionist();
        receptionist.setUser(user);
        receptionist.setShift(request.getShift());
        receptionist.setDeskNumber(request.getDeskNumber());

        // Save receptionist
        receptionist = receptionistRepository.save(receptionist);

        try {
            emailService.sendReceptionistWelcomeEmail(
                    user.getEmail(),
                    user.getFullName(),
                    user.getUsername(),
                    request.getPassword(), // Temporary password
                    receptionist.getShift().toString(),
                    receptionist.getDeskNumber()
            );
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to receptionist: " + e.getMessage());
            // Continue anyway - receptionist was created successfully
        }

        return convertToResponse(receptionist, "Receptionist created successfully");
    }

    public List<ReceptionistResponse> getAllReceptionists() {
        return receptionistRepository.findAll().stream()
                .map(receptionist -> convertToResponse(receptionist, null))
                .collect(Collectors.toList());
    }

    public List<ReceptionistResponse> getActiveReceptionists() {
        return receptionistRepository.findByUser_IsActiveTrue().stream()
                .map(receptionist -> convertToResponse(receptionist, null))
                .collect(Collectors.toList());
    }

    public List<ReceptionistResponse> getReceptionistsByShift(Receptionist.Shift shift) {
        return receptionistRepository.findByShift(shift).stream()
                .map(receptionist -> convertToResponse(receptionist, null))
                .collect(Collectors.toList());
    }

    public ReceptionistResponse getReceptionistById(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found"));
        return convertToResponse(receptionist, null);
    }

    @Transactional
    public ReceptionistResponse updateReceptionist(Long id, CreateReceptionistRequest request) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found"));

        User user = receptionist.getUser();

        // Update user information
        if (!user.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (!user.getPhone().equals(request.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new RuntimeException("Phone number already exists");
            }
            user.setPhone(request.getPhone().trim());
        }

        user.setFullName(request.getFullName().trim());
        user.setIsActive(request.getIsActive());
        user.setGender(User.Gender.valueOf(request.getGender()));

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            String passwordError = ValidationUtil.validatePassword(request.getPassword());
            if (passwordError != null) {
                throw new RuntimeException(passwordError);
            }
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);


        receptionist.setShift(request.getShift());
        receptionist.setDeskNumber(request.getDeskNumber());

        receptionist = receptionistRepository.save(receptionist);

        return convertToResponse(receptionist, "Receptionist updated successfully");
    }

    @Transactional
    public void deleteReceptionist(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Receptionist not found"));

        // Soft delete by deactivating user account
        User user = receptionist.getUser();
        user.setIsActive(false);
        userRepository.save(user);
    }

    // Helper method to convert Receptionist to Response
    private ReceptionistResponse convertToResponse(Receptionist receptionist, String message) {
        ReceptionistResponse response = new ReceptionistResponse();
        response.setId(receptionist.getId());
        response.setUserId(receptionist.getUser().getId());
        response.setUsername(receptionist.getUser().getUsername());
        response.setFullName(receptionist.getUser().getFullName());
        response.setEmail(receptionist.getUser().getEmail());
        response.setPhone(receptionist.getUser().getPhone());
        response.setGender(receptionist.getUser().getGender());
        response.setShift(receptionist.getShift());
        response.setDeskNumber(receptionist.getDeskNumber());
        response.setJoinedDate(receptionist.getJoinedDate());
        response.setIsActive(receptionist.getUser().getIsActive());
        response.setManagedAppointmentsCount(
                receptionist.getManagedAppointments() != null ?
                        receptionist.getManagedAppointments().size() : 0
        );
        response.setMessage(message);
        return response;
    }
}