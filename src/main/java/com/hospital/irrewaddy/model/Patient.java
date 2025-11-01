package com.hospital.irrewaddy.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "patient")
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    public String bloodGroup;

    //Relationships
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> appointments;

    public Patient() {
    }
    public Patient(Long id, User user, String bloodGroup,
                   String emergencyContactName, String emergencyContactPhone,
                   String allergies, String medicalHistory) {
        this.id = id;
        this.bloodGroup = bloodGroup;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public List<Appointment> getAppointments() { return appointments; }
    public void setAppointments(List<Appointment> appointments) {
        this.appointments = appointments;
    }

}