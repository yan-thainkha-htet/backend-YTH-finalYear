package com.hospital.irrewaddy.service;

import com.hospital.irrewaddy.dto.AuthResponse;
import com.hospital.irrewaddy.dto.LoginRequest;
import com.hospital.irrewaddy.dto.RegisterRequest;
import com.hospital.irrewaddy.model.Patient;
import com.hospital.irrewaddy.model.User;
import com.hospital.irrewaddy.repository.PatientRepository;
import com.hospital.irrewaddy.repository.UserRepository;
import com.hospital.irrewaddy.security.JwtUtil;
import com.hospital.irrewaddy.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        // Generate username from email
        String username = request.generateUsername();

        // Check username uniqueness
        if (userRepository.existsByUsername(username)) {
            // If username exists, append a number
            int counter = 1;
            while (userRepository.existsByUsername(username + counter)) {
                counter++;
            }
            username = username + counter;
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
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setFullName(request.getFullName());
        user.setRole(User.UserRole.PATIENT); // Auto-assign PATIENT role
        user.setIsActive(true);

        // Save user first
        user = userRepository.save(user);

        // Create Patient entity with additional information
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(request.getDateOfBirth());
        patient.setGender(request.getGender());
        patient.setAddress(request.getAddress());
        // Other patient fields can be null initially and filled later

        // Save patient
        patientRepository.save(patient);

        // Generate token
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                "Registration successful"
        );
    }

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

            return new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    "Login successful"
            );
        } catch (BadCredentialsException e) {
            throw new RuntimeException("Invalid username or password");
        }
    }
}