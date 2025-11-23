package com.hospital.irrewaddy.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "doctor")
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
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

    // Many-to-Many relationship with Specializ ation
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "doctor_specialization",
            joinColumns = @JoinColumn(name = "doctor_id"),
            inverseJoinColumns = @JoinColumn(name = "specialization_id")
    )
    private Set<Specialization> specializations = new HashSet<>();

    public Set<Specialization> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(Set<Specialization> specializations) {
        this.specializations = specializations;
    }

    // Constructors
    public Doctor() {
    }


    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
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

    // Helper methods to manage the bidirectional relationship
    public void addSpecialization(Specialization specialization) {
        this.specializations.add(specialization);
        specialization.getDoctors().add(this);
    }

    public void removeSpecialization(Specialization specialization) {
        this.specializations.remove(specialization);
        specialization.getDoctors().remove(this);
    }

    public void clearSpecializations() {
        for (Specialization specialization : new HashSet<>(this.specializations)) {
            removeSpecialization(specialization);
        }
    }
}