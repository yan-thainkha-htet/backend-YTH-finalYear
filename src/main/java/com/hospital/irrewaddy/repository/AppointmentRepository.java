package com.hospital.irrewaddy.repository;

import com.hospital.irrewaddy.model.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Find appointments by patient
    List<Appointment> findByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);

    // Find appointments by doctor
    List<Appointment> findByDoctorIdOrderByAppointmentDateAscAppointmentTimeAsc(Long doctorId);

    // Find appointments by department
    List<Appointment> findByDepartmentId(Long departmentId);

    // Find appointments by status
    List<Appointment> findByStatus(Appointment.AppointmentStatus status);

    // Find appointments by date
    List<Appointment> findByAppointmentDate(LocalDate date);

    // Find appointments by doctor and date
    List<Appointment> findByDoctorIdAndAppointmentDate(Long doctorId, LocalDate date);

    // Find appointments by patient and status
    List<Appointment> findByPatientIdAndStatus(Long patientId, Appointment.AppointmentStatus status);

    // Find appointments by doctor and status
    List<Appointment> findByDoctorIdAndStatus(Long doctorId, Appointment.AppointmentStatus status);

    long countByStatus(Appointment.AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.status = PENDING AND a.doctor.id = :doctorId")
    long countByStatusAndDoctorId(Appointment.AppointmentStatus status, @Param("doctorId") Long doctorId);

    // Check if doctor has appointment at specific date and time
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(Long doctorId, LocalDate date, java.time.LocalTime time);

    // Get max token number for a doctor on a specific date
    @Query("SELECT MAX(a.tokenNumber) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date")
    Integer getMaxTokenNumberForDoctorAndDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    // Find upcoming appointments for patient
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.appointmentDate >= :currentDate ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByPatient(@Param("patientId") Long patientId, @Param("currentDate") LocalDate currentDate);

    // Find upcoming appointments for doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate >= :currentDate ORDER BY a.appointmentDate ASC, a.appointmentTime ASC")
    List<Appointment> findUpcomingAppointmentsByDoctor(@Param("doctorId") Long doctorId, @Param("currentDate") LocalDate currentDate);

    // Find today's appointments for doctor
    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date ORDER BY a.appointmentTime ASC")
    List<Appointment> findTodayAppointmentsByDoctor(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    long countByAppointmentDate(LocalDate date);

    long countByAppointmentDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(DISTINCT a.patient.id) FROM Appointment a WHERE a.doctor.id = :doctorId")
    long countUniquePatientsByDoctorId(@Param("doctorId") Long doctorId);
}