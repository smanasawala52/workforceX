package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobBrowseResponse;
import com.workforcex.backend.entity.EmployerProfile;
import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spiral 2: lets workers browse all posted jobs.
 * Separated from JobService (which is employer-only CRUD)
 * to keep employer-side ownership logic isolated.
 */
@Service
@RequiredArgsConstructor
public class JobBrowseService {

    private final JobRepository jobRepository;
    private final EmployerProfileRepository employerProfileRepository;

    public List<JobBrowseResponse> getAllJobs() {
        // Look up the employer's company name if they filled in a profile
        return jobRepository.findAll().stream()
                .map(JobBrowseResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
