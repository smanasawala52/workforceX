package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A job posting created by an Employer.
 * skillsRequired and location stored as comma-separated strings —
 * matching engine splits on commas to compare.
 */
@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;
     */
    @Column(name = "employer_id", nullable = false)
    private UUID employerId;

    @Column(nullable = false)
    private String title;

    private String companyName; // used while fetching data to avoid employer table call
    private String employerMobileNumber;  // used while fetching data to avoid employer table call

    private String skillsRequired1;      // comma-separated, e.g. "security,patrolling"
    private String skillsRequired2;      // comma-separated, e.g. "security,patrolling"
    private String skillsRequired3;      // comma-separated, e.g. "security,patrolling"
    private String skillsRequired4;      // comma-separated, e.g. "security,patrolling"
    private String skillsRequired5;      // comma-separated, e.g. "security,patrolling"

    private Integer experienceRequired; // years

    // Comma-separated cities, e.g. "Mumbai,Pune,Thane"
    private String location;

    private Double salaryMin;           // salary range lower bound (₹/month)
    private Double salaryMax;           // salary range upper bound (₹/month)

    private Integer openPositions;      // number of vacancies

    @Column(length = 2000)
    private String description;
}
