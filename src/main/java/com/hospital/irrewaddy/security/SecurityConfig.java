package com.hospital.irrewaddy.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers(
                                "/hospital/api/auth/**",
                                "/hospital/api/auth/forgot-password",
                                "/hospital/api/auth/verify-otp",
                                "/hospital/api/auth/reset-password",
                                "/hospital/api/auth/resend-otp"
                        ).permitAll()
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

                        // Admin routes
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