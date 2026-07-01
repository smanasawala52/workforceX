package com.workforcex.backend.controller;

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
     * GET /api/matching/{jobId}
     * Returns workers ranked by score (Skills 40%, Experience 30%, Location 20%, Salary 10%).
     * Only the employer who owns the job can call this.
     */
    @GetMapping("/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<MatchedWorkerResponse>> getMatchedWorkers(
            Authentication authentication,
            @PathVariable UUID jobId
    ) {
        List<MatchedWorkerResponse> results =
                matchingService.getMatchedWorkers(authentication.getName(), jobId);
        return ResponseEntity.ok(results);
    }
}
