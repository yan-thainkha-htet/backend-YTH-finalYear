package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {

    Optional<Receptionist> findByUserId(Long userId);

    List<Receptionist> findByShift(Receptionist.Shift shift);

    List<Receptionist> findByUser_IsActiveTrue();
}