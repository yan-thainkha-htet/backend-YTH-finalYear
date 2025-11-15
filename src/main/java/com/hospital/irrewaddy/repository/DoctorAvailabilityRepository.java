package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {
    List<DoctorAvailability> findByDoctorId(Long doctorId);

    Optional<DoctorAvailability> findByDoctorIdAndDayOfWeek(Long doctorId, DoctorAvailability.DayOfWeek dayOfWeek);

    List<DoctorAvailability> findByDoctorIdAndIsAvailable(Long doctorId, Boolean isAvailable);

    boolean existsByDoctorIdAndDayOfWeek(Long doctorId, DoctorAvailability.DayOfWeek dayOfWeek);
}