package com.hospital.irrewaddy.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "receptionist")
public class Receptionist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "employee_id", unique = true, length = 50)
    private String employeeId;

    @Column(name = "shift", length = 20)
    private String shift; // MORNING, AFTERNOON, EVENING, NIGHT

    @Column(name = "desk_number")
    private Integer deskNumber;

    @CreationTimestamp
    @Column(name = "joined_date")
    private LocalDateTime joinedDate;

    @Column(name = "is_on_duty")
    private Boolean isOnDuty = true;

    // Relationships
    @OneToMany(mappedBy = "receptionist")
    private List<Appointment> managedAppointments;

    // Constructors
    public Receptionist() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public Integer getDeskNumber() {
        return deskNumber;
    }

    public void setDeskNumber(Integer deskNumber) {
        this.deskNumber = deskNumber;
    }

    public LocalDateTime getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDateTime joinedDate) {
        this.joinedDate = joinedDate;
    }

    public Boolean getIsOnDuty() {
        return isOnDuty;
    }

    public void setIsOnDuty(Boolean isOnDuty) {
        this.isOnDuty = isOnDuty;
    }

    public List<Appointment> getManagedAppointments() {
        return managedAppointments;
    }

    public void setManagedAppointments(List<Appointment> managedAppointments) {
        this.managedAppointments = managedAppointments;
    }
}