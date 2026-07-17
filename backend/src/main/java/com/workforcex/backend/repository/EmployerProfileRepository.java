package com.workforcex.backend.repository;

import com.workforcex.backend.entity.EmployerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployerProfileRepository extends JpaRepository<EmployerProfile, UUID> {

    Optional<EmployerProfile> findByUserId(UUID userId);

    List<EmployerProfile> findByAddressContaining(String address);
}
