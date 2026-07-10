package com.workforcex.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "employer_verifications")
@Getter
@Setter
@NoArgsConstructor
public class EmployerVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "verification_id", nullable = false)
    private Verification verification;

    @ManyToOne
    @JoinColumn(name = "employer_id", nullable = false)
    private User employer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus status = VerificationStatus.PENDING;

    private String reviewerComments;
}
