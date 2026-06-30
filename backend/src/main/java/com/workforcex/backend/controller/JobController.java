package com.workforcex.backend.controller;

import com.workforcex.backend.dto.JobRequest;
import com.workforcex.backend.dto.JobResponse;
import com.workforcex.backend.entity.Job;
import com.workforcex.backend.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponse> create(Authentication authentication, @Valid @RequestBody JobRequest request) {
        Job job = jobService.createJob(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(JobResponse.fromEntity(job));
    }

    @PutMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponse> update(
            Authentication authentication,
            @PathVariable UUID jobId,
            @Valid @RequestBody JobRequest request
    ) {
        Job job = jobService.updateJob(authentication.getName(), jobId, request);
        return ResponseEntity.ok(JobResponse.fromEntity(job));
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable UUID jobId) {
        jobService.deleteJob(authentication.getName(), jobId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobResponse> getOne(Authentication authentication, @PathVariable UUID jobId) {
        Job job = jobService.getJobById(authentication.getName(), jobId);
        return ResponseEntity.ok(JobResponse.fromEntity(job));
    }

    @GetMapping
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobResponse>> getMine(Authentication authentication) {
        List<JobResponse> jobs = jobService.getJobsForEmployer(authentication.getName())
                .stream()
                .map(JobResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(jobs);
    }
}
