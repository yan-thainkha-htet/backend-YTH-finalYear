package com.hospital.irrewaddy.model;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "specialization")
public class Specialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name;
    @ManyToMany(mappedBy = "specializations", fetch = FetchType.LAZY)
    private Set<Doctor> doctors = new HashSet<>();

    public Specialization() {
    }
    public Specialization(String name) { this.name = name; }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Doctor> getDoctors() {
        return doctors;
    }
    public void setDoctors(Set<Doctor> doctors) {
        this.doctors = doctors;
    }

}
