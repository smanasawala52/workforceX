package com.workforcex.backend.controller;

import com.workforcex.backend.dto.JobBrowseResponse;
import com.workforcex.backend.service.JobBrowseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobBrowseController {

    private final JobBrowseService jobBrowseService;

    /**
     * GET /api/jobs/browse
     * Returns all posted jobs. Accessible to WORKER role.
     * Includes company name so workers know who posted the job.
     */
    @GetMapping("/browse")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<JobBrowseResponse>> browseJobs() {
        return ResponseEntity.ok(jobBrowseService.getAllJobs());
    }
}
