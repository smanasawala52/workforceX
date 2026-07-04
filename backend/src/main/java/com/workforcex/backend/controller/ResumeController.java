package com.workforcex.backend.controller;

import com.workforcex.backend.dto.ResumeParseResponse;
import com.workforcex.backend.service.ResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/worker/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * POST /api/worker/resume
     * Accepts a PDF file, parses it, auto-extracts skills and experience,
     * saves to the worker's profile and returns the parsed result.
     *
     * Content-Type: multipart/form-data
     * Field name: file
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<ResumeParseResponse> uploadResume(
            Authentication authentication,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        ResumeParseResponse response = resumeService.parseAndSave(
                authentication.getName(), file);
        return ResponseEntity.ok(response);
    }
}
