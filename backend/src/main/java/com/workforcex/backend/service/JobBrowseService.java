package com.workforcex.backend.service;

import com.workforcex.backend.dto.JobBrowseResponse;
import com.workforcex.backend.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobBrowseService {

    private final JobRepository jobRepository;

    @Transactional(readOnly = true)
    public List<JobBrowseResponse> getAllJobs() {
        return jobRepository.findAllWithEmployerAndProfile().stream()
                .map(JobBrowseResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
