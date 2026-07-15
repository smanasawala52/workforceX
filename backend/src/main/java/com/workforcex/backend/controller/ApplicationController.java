package com.workforcex.backend.controller;

import com.workforcex.backend.dto.JobApplicationResponse;
import com.workforcex.backend.entity.ApplicationStatus;
import com.workforcex.backend.service.ApplicationService;
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
            @RequestHeader(value = "X-Country-Code", defaultValue = "+91") String countryCode,
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(countryCode, auth.getName(), jobId));
    }

    /**
     * Employer: proactively offer a job to a matched worker.
     * POST /api/applications/offer?jobId=...&workerId=...
     */
    @PostMapping("/offer")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> offerJob(
            Authentication auth,
            @RequestHeader(value = "X-Country-Code", defaultValue = "+91") String countryCode,
            @RequestParam UUID jobId,
            @RequestParam UUID workerId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.offerJob(countryCode, auth.getName(), jobId, workerId));
    }

    /** Worker: view all jobs I applied to. GET /api/applications/my */
    @GetMapping("/my")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<JobApplicationResponse>> getMyApplications(
            Authentication auth,
            @RequestHeader(value = "X-Country-Code", defaultValue = "+91") String countryCode
    ) {
        return ResponseEntity.ok(applicationService.getMyApplications(countryCode, auth.getName()));
    }

    /**
     * Employer: view all applicants for a job.
     * GET /api/applications/job/{jobId}
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<JobApplicationResponse>> getApplicationsForJob(
            Authentication auth,
            @RequestHeader(value = "X-Country-Code", defaultValue = "+91") String countryCode,
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(
                applicationService.getApplicationsForJob(countryCode, auth.getName(), jobId));
    }

    /**
     * Employer: shortlist or reject an applicant.
     * PUT /api/applications/{applicationId}/status?status=SHORTLISTED
     */
    @PutMapping("/{applicationId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<JobApplicationResponse> updateStatus(
            Authentication auth,
            @RequestHeader(value = "X-Country-Code", defaultValue = "+91") String countryCode,
            @PathVariable UUID applicationId,
            @RequestParam ApplicationStatus status
    ) {
        return ResponseEntity.ok(
                applicationService.updateStatus(countryCode, auth.getName(), applicationId, status));
    }
}
