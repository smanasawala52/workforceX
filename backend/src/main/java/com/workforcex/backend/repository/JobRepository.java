package com.workforcex.backend.repository;

import com.workforcex.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    List<Job> findAllByEmployerId(UUID employerId);
}
