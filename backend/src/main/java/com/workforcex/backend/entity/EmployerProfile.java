package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * Employer-specific profile data. One-to-one with User.
 * Only companyName + contactPerson are meaningful for Spiral 1;
 * email/address optional. Everything else (logo, GST, verification) is future scope.
 */
@Entity
@Table(name = "employer_profiles")
@Getter
@Setter
@NoArgsConstructor
public class EmployerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String companyName;
    private String contactPerson;
    private String email;
    private String address;
}
