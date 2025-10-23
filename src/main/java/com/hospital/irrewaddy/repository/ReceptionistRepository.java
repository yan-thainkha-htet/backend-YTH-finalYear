package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Receptionist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {

    Optional<Receptionist> findByUserId(Long userId);

    Optional<Receptionist> findByEmployeeId(String employeeId);

    Boolean existsByEmployeeId(String employeeId);

    List<Receptionist> findByShift(String shift);

    List<Receptionist> findByIsOnDutyTrue();

    List<Receptionist> findByUser_IsActiveTrue();
}