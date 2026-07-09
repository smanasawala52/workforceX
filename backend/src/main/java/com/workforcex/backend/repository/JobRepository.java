package com.workforcex.backend.repository;

import com.workforcex.backend.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {

    @Query("SELECT j FROM Job j")
    List<Job> findAllWithEmployerAndProfile();

    @Query("SELECT j FROM Job j WHERE j.employerId = :employerId")
    List<Job> findAllByEmployerId(UUID employerId);
}
