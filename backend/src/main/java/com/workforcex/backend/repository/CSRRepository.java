package com.workforcex.backend.repository;

import com.workforcex.backend.entity.CSR;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CSRRepository extends JpaRepository<CSR, UUID> {

    Optional<CSR> findByUserId(UUID userId);
}
