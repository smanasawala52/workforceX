package com.workforcex.backend.controller;

import com.workforcex.backend.dto.CandidateSearchRequest;
import com.workforcex.backend.dto.CandidateSearchResponse;
import com.workforcex.backend.dto.MatchedWorkerResponse;
import com.workforcex.backend.service.MatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    /**
     * Spiral 1: GET /api/matching/{jobId}
     * Ranks all workers against a specific job.
     */
    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<MatchedWorkerResponse>> getMatchedWorkers(
            Authentication authentication,
            @PathVariable UUID jobId
    ) {
        return ResponseEntity.ok(
                matchingService.getMatchedWorkers(authentication.getName(), jobId));
    }

    /**
     * Spiral 2: POST /api/matching/search
     * Free-text candidate search with optional filters.
     * All params optional. Returns ranked workers with score breakdown.
     *
     * Example: /api/matching/search with body {"skills":"driving,security","city":"Mumbai","experienceMin":2,"salaryMax":20000}
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<CandidateSearchResponse>> search(
            Authentication authentication,
            @RequestBody CandidateSearchRequest request
    ) {
        return ResponseEntity.ok(matchingService.search(request));
    }
}
