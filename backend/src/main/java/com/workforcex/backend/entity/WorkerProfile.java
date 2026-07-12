package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "worker_profiles")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class WorkerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*@OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;*/
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String userMobileNumber;
    private String name;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String address;
    private String city;
    private String state;

    private String skill1;
    private String skill2;
    private String skill3;
    private String skill4;
    private String skill5;
    private Integer experience;
    private Double preferredSalary;
    private String availability;
    private String languages;

    private String resumeFileName;

    @Column(length = 10000)
    private String resumeText;

    @Column(length = 2000)
    private String resumeExtractedSkills;

    @Column(length = 3000)
    private String description;
}
