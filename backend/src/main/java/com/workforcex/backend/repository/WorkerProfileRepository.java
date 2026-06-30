package com.workforcex.backend.repository;

import com.workforcex.backend.entity.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, UUID> {

    Optional<WorkerProfile> findByUserId(UUID userId);
}
