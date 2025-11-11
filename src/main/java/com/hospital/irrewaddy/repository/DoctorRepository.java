package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Find doctor by user ID
    Optional<Doctor> findByUserId(Long userId);

    // Find doctors by department
    List<Doctor> findByDepartmentId(Long departmentId);


    // Find active doctors (through user relationship)
    List<Doctor> findByUser_IsActiveTrue();
}