package com.hospital.irrewaddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column(name = "specialization", length = 100)
    private String specialization;
    @Column(name = "qualification", length = 255)
    private String qualification;
    @Column(name = "experience_years")
    private Integer experienceYears;
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;


    // Relationships
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<DoctorAvailability> availabilities;
    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    // Constructors
    public Doctor() {
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
    }
    public String getSpecialization() {
        return specialization;
    }
    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }
    public String getQualification() {
        return qualification;
    }
    public void setQualification(String qualification) {
        this.qualification = qualification;
    }
    public Integer getExperienceYears() {
        return experienceYears;
    }
    public void setExperienceYears(Integer experienceYears) {
        this.experienceYears = experienceYears;
    }
    public String getBio() {
        return bio;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }


    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Department getDepartment() {
        return department;
    }
    public void setDepartment(Department department) { this.department = department; }
    public List<DoctorAvailability> getAvailabilities() { return availabilities; }
    public void setAvailabilities(List<DoctorAvailability> availabilities) { this.availabilities = availabilities; }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) { this.appointments = appointments; }
}