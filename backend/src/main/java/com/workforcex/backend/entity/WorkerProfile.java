package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Worker-specific profile data. One-to-one with User.
 * Everything here is optional except the link to the owning User -
 * matches the registration philosophy (mobile number is the only requirement).
 */
@Entity
@Table(name = "worker_profiles")
@Getter
@Setter
@NoArgsConstructor
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String address;
    private String city;
    private String state;

    // Comma-separated for Spiral 1 simplicity, e.g. "driving,security,cleaning"
    private String skills;

    // Years of experience
    private Integer experience;

    private Double preferredSalary;

    // Resume fields (Spiral 2) - stores parsed text, not the raw file
    private String resumeFileName;

    @Column(length = 10000)
    private String resumeText; // full extracted text from PDF

    @Column(length = 2000)
    private String resumeExtractedSkills; // auto-detected skills from resume

    // Photo and video are future scope - not part of Spiral 1
}
