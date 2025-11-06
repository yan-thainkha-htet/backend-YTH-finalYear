package com.hospital.irrewaddy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use allowedOriginPatterns instead of allowedOrigins when credentials = true
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));

        // Allow all HTTP methods including OPTIONS
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));

        // Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Allow credentials (required for JWT tokens)
        configuration.setAllowCredentials(true);

        // Expose Authorization header so frontend can read it
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ENABLE CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // CRITICAL: Allow all OPTIONS requests (CORS preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Error endpoint
                        .requestMatchers("/error").permitAll()

                        // Auth endpoints - PUBLIC ACCESS
                        .requestMatchers(
                                "/hospital/api/auth/login",
                                "/hospital/api/auth/register",
                                "/hospital/api/auth/forgot-password",
                                "/hospital/api/auth/verify-otp",
                                "/hospital/api/auth/reset-password",
                                "/hospital/api/auth/resend-otp",
                                "/hospital/api/auth/send-otp"  // Added for admin setup
                        ).permitAll()

                        // Auth endpoints that require authentication
                        .requestMatchers("/hospital/api/auth/change-password").authenticated()

                        // Departments
                        .requestMatchers("/hospital/api/departments", "/hospital/api/departments/active").permitAll()
                        .requestMatchers("/hospital/api/departments/{id}", "/hospital/api/departments/name/{name}").permitAll()
                        .requestMatchers("/hospital/api/departments/**").hasRole("ADMIN")

                        // Doctors
                        .requestMatchers("/hospital/api/doctors").permitAll()
                        .requestMatchers("/hospital/api/doctors/{id}").permitAll()
                        .requestMatchers("/hospital/api/doctors/**").hasRole("ADMIN")

                        // Receptionists
                        .requestMatchers("/hospital/api/receptionists/on-duty").hasAnyRole("ADMIN", "RECEPTIONIST")
                        .requestMatchers("/hospital/api/receptionists/**").hasRole("ADMIN")

                        // Appointments
                        .requestMatchers("/hospital/api/appointments").hasAnyRole("PATIENT", "ADMIN")
                        .requestMatchers("/hospital/api/appointments/**").authenticated()

                        // ⭐ UPDATED: Super Admin routes

                        .requestMatchers("/hospital/api/superadmin/**").authenticated()



                        // ⭐ NEW: Admin setup flow endpoints (must be BEFORE general admin routes)
                        //.requestMatchers("/hospital/api/admin/complete-profile").hasRole("ADMIN")
                        //.requestMatchers("/hospital/api/admin/setup-status").hasRole("ADMIN")
                        .requestMatchers("/hospital/api/admin/**").hasRole("ADMIN")

                        // Patient routes
                        .requestMatchers("/hospital/api/patient/**").hasRole("PATIENT")

                        // Receptionist routes
                        .requestMatchers("/hospital/api/receptionist/**").hasRole("RECEPTIONIST")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}