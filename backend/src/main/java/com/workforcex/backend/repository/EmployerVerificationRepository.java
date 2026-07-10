package com.workforcex.backend.repository;

import com.workforcex.backend.entity.EmployerVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployerVerificationRepository extends JpaRepository<EmployerVerification, UUID> {
}
