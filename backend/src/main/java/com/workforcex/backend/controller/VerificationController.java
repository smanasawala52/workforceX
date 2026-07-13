package com.workforcex.backend.controller;

import com.workforcex.backend.dto.DocumentResponse;
import com.workforcex.backend.entity.Document;
import com.workforcex.backend.entity.DocumentType;
import com.workforcex.backend.entity.EmployerVerification;
import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import com.workforcex.backend.service.VerificationService;
import com.workforcex.backend.service.storage.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    // Present for any non-"prod" profile (LocalFileStorageService); null in
    // "prod", where documents are served via signed Supabase URLs returned
    // directly in DocumentResponse.fileUrl instead.
    @Autowired(required = false)
    private LocalFileStorageService localFileStorageService;

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
     * Employer: Get all pending verifications.
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<Verification>> getPendingVerifications() {
        return ResponseEntity.ok(verificationService.getPendingVerifications());
    }

    /**
     * Employer: Get all documents for a specific worker.
     */
    @GetMapping("/worker/{workerId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<Verification>> getWorkerDocuments(@PathVariable UUID workerId) {
        return ResponseEntity.ok(verificationService.getVerificationsForWorker(workerId));
    }

    /**
     * Worker: Get the actual list of documents they've uploaded (filenames,
     * types, and a URL to view each one) - distinct from /status above,
     * which only returns the coarse per-category verification state.
     */
    @GetMapping("/documents")
    @PreAuthorize("hasRole('WORKER')")
    public ResponseEntity<List<DocumentResponse>> getMyDocuments(Authentication auth) {
        return ResponseEntity.ok(verificationService.getDocumentsForUserByMobile(auth.getName()));
    }

    /**
     * Employer: Get the actual documents a specific worker uploaded, so the
     * employer profile view can show/open the real files rather than just
     * an aggregate verification status.
     */
    @GetMapping("/worker/{workerId}/documents")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<List<DocumentResponse>> getWorkerDocumentFiles(@PathVariable UUID workerId) {
        return ResponseEntity.ok(verificationService.getDocumentsForWorker(workerId));
    }

    /**
     * Streams the raw bytes of a document. Only wired up when local disk
     * storage (dev profile) is active - in prod, DocumentResponse.fileUrl is
     * already a directly-usable signed Supabase Storage URL, so clients
     * never need to hit this endpoint there.
     */
    @GetMapping("/documents/{documentId}/raw")
    public ResponseEntity<Resource> getDocumentFile(@PathVariable UUID documentId, Authentication auth) {
        if (localFileStorageService == null) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }

        boolean isEmployer = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EMPLOYER"));

        Document document;
        try {
            document = verificationService.getDocumentForAccess(documentId, auth.getName(), isEmployer);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }

        try {
            var path = localFileStorageService.resolvePath(document.getStorageKey());
            Resource resource = new FileSystemResource(path);
            String contentType = Files.probeContentType(path);
            return ResponseEntity.ok()
                    .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "inline; filename=\"" + document.getFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Admin/Employer: Update the status of a verification record.
     */
    @PutMapping("/{verificationId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<EmployerVerification> updateVerificationStatus(
            Authentication auth,
            @PathVariable UUID verificationId,
            @RequestParam("status") VerificationStatus newStatus,
            @RequestBody(required = false) String comments
    ) {
        return ResponseEntity.ok(verificationService.updateEmployerVerificationStatus(auth.getName(), verificationId, newStatus, comments));
    }
}
