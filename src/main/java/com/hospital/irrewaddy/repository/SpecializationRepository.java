package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpecializationRepository extends JpaRepository<Specialization, Long> {
    Optional<Specialization> findByName(String name);

    Optional<Specialization> findById(Long id);
    boolean existsByName(String name);
}