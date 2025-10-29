package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Admin;
import com.hospital.irrewaddy.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUserId(Long userId);
}
