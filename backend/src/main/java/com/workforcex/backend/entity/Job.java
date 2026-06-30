package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * A job posting created by an Employer.
 * skillsRequired stored as comma-separated string, same approach as WorkerProfile.skills -
 * matching engine will split both on commas to compare.
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

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Column(nullable = false)
    private String title;

    private String skillsRequired; // comma-separated, e.g. "driving,security"

    private Integer experienceRequired; // years

    private String location;

    private Double salary;

    @Column(length = 2000)
    private String description;
}
