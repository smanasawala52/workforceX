package com.workforcex.backend.repository;

import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.job j LEFT JOIN FETCH ja.worker WHERE ja.worker.id = :workerId")
    List<JobApplication> findAllByWorkerId(UUID workerId);

    @Query("SELECT ja FROM JobApplication ja LEFT JOIN FETCH ja.job j LEFT JOIN FETCH ja.worker WHERE ja.job.id = :jobId")
    List<JobApplication> findAllByJobId(UUID jobId);

    boolean existsByJobIdAndWorkerId(UUID jobId, UUID workerId);

    Optional<JobApplication> findByJobIdAndWorkerId(UUID jobId, UUID workerId);

    List<JobApplication> findAllByJobIdAndStatus(UUID jobId, ApplicationStatus status);
}
