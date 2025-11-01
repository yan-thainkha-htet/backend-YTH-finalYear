package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.EmailOTP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailOTPRepository extends JpaRepository<EmailOTP, Long> {
    Optional<EmailOTP> findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(String email);
    void deleteByEmail(String email);
}