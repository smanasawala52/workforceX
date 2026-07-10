package com.workforcex.backend.repository;

import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface VerificationRepository extends JpaRepository<Verification, UUID> {
    List<Verification> findByUserId(UUID userId);
    List<Verification> findByStatus(VerificationStatus status);
}
