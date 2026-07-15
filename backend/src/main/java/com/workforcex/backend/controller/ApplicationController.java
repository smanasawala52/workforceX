package com.workforcex.backend.controller;

import com.workforcex.backend.dto.JobApplicationResponse;
import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.service.ApplicationService;
import com.workforcex.backend.util.PhoneNumbers;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /** Worker: apply to a job. POST /api/applications/{jobId} */
    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<JobApplicationResponse> apply(
            Authentication auth,
            @PathVariable UUID jobId
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(split.countryCode(), split.mobileNumber(), jobId));
    }

    /**
     * Employer: proactively offer a job to a matched worker.
     * POST /api/applications/offer?jobId=...&workerId=...
     */
    @PostMapping("/offer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> offerJob(
            Authentication auth,
            @RequestParam UUID jobId,
            @RequestParam UUID workerId
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.offerJob(split.countryCode(), split.mobileNumber(), jobId, workerId));
    }

    /** Worker: view all jobs I applied to. GET /api/applications/my */
    @GetMapping("/my")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(
            Authentication auth
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(auth.getName());
        return ResponseEntity.ok(applicationService.getMyApplications(split.countryCode(), split.mobileNumber()));
    }

    /**
     * Employer: view all applicants for a job.
     * GET /api/applications/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForJob(
            Authentication auth,
            @PathVariable UUID jobId
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(auth.getName());
        return ResponseEntity.ok(
                applicationService.getApplicationsForJob(split.countryCode(), split.mobileNumber(), jobId));
    }

    /**
     * Employer: shortlist or reject an applicant.
     * PUT /api/applications/{applicationId}/status?status=SHORTLISTED
     */
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            Authentication auth,
            @PathVariable UUID applicationId,
            @RequestParam ApplicationStatus status
    ) {
        PhoneNumbers.Split split = PhoneNumbers.split(auth.getName());
        return ResponseEntity.ok(
                applicationService.updateStatus(split.countryCode(), split.mobileNumber(), applicationId, status));
    }
}
