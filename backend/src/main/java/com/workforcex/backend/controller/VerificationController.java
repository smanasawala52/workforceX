package com.workforcex.backend.controller;

import com.workforcex.backend.entity.Document;
import com.workforcex.backend.entity.DocumentType;
import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import com.workforcex.backend.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    /**
     * Worker: Get my current verification status for all types.
     */
    @GetMapping("/status")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<Verification>> getMyVerificationStatus(Authentication auth) {
        return ResponseEntity.ok(verificationService.getVerificationStatusForUser(auth.getName()));
    }

    /**
     * Worker: Upload a document for verification.
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<Document> uploadDocument(
            Authentication auth,
            @RequestParam("type") DocumentType documentType,
            @RequestParam("file") MultipartFile file
    ) {
        return ResponseEntity.ok(verificationService.uploadDocument(auth.getName(), documentType, file));
    }

    /**
     * Admin/Employer: Update the status of a verification record.
     */
    @PutMapping("/{verificationId}/status")
    // @PreAuthorize is removed; security is now handled centrally in SecurityConfig
    public ResponseEntity<Verification> updateVerificationStatus(
            @PathVariable UUID verificationId,
            @RequestParam("status") VerificationStatus newStatus,
            @RequestBody(required = false) String comments
    ) {
        return ResponseEntity.ok(verificationService.updateVerificationStatus(verificationId, newStatus, comments));
    }
}
