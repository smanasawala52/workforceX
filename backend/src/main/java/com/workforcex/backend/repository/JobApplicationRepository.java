package com.workforcex.backend.repository;

import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    // Worker: all jobs I applied to
    List<JobApplication> findAllByWorkerId(UUID workerId);

    // Employer: all applicants for a specific job
    List<JobApplication> findAllByJobId(UUID jobId);

    // Check for duplicate application
    boolean existsByJobIdAndWorkerId(UUID jobId, UUID workerId);

    // Find a specific application
    Optional<JobApplication> findByJobIdAndWorkerId(UUID jobId, UUID workerId);

    // Employer: filter applicants by status
    List<JobApplication> findAllByJobIdAndStatus(UUID jobId, ApplicationStatus status);
}
