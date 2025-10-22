package com.hospital.irrewaddy.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "admin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_level", length = 20)
    private com.hospital.irrewaddy.model.Admin.AccessLevel accessLevel;

    // Enum for Access Level
    public enum AccessLevel {
        SUPER_ADMIN,
        ADMIN
    }
}

