package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

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

    private String skills;
    private Integer experience;
    private Double preferredSalary;
    private String availability;
    private String languages;

    private String resumeFileName;

    @Column(length = 10000)
    private String resumeText;

    @Column(length = 2000)
    private String resumeExtractedSkills;
}
