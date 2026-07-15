package com.workforcex.backend.service;

import com.workforcex.backend.dto.DocumentResponse;
import com.workforcex.backend.entity.Document;
import com.workforcex.backend.entity.DocumentType;
import com.workforcex.backend.entity.EmployerVerification;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import com.workforcex.backend.entity.VerificationType;
import com.workforcex.backend.repository.DocumentRepository;
import com.workforcex.backend.repository.EmployerVerificationRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.VerificationRepository;
import com.workforcex.backend.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final VerificationRepository verificationRepository;
    private final EmployerVerificationRepository employerVerificationRepository;
    private final FileStorageService fileStorageService;

    private User findUserByMobile(String countryCode, String mobileNumber) {
        return userRepository.findByCountryCodeAndMobileNumber(countryCode, mobileNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public List<Verification> getVerificationStatusForUser(String countryCode, String userMobile) {
        User user = findUserByMobile(countryCode, userMobile);
        return verificationRepository.findByUserId(user.getId());
    }

    public Document uploadDocument(String countryCode, String userMobile, DocumentType documentType, MultipartFile file) {
        User user = findUserByMobile(countryCode, userMobile);

        String storageKey;
        try {
            storageKey = fileStorageService.store(file, user.getId());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store uploaded file", e);
        }

        Document document = new Document();
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setFileName(file.getOriginalFilename());
        document.setStorageKey(storageKey);
        documentRepository.save(document);

        // When a document is uploaded, update the corresponding verification status to "SUBMITTED"
        updateVerificationStatusOnUpload(user, documentType);

        return document;
    }

    /**
     * Worker: list of their own uploaded documents (with a live view URL) -
     * this is what actually backs the "list of documents" the worker sees,
     * as opposed to the coarse per-category status in Verification.
     */
    public List<DocumentResponse> getDocumentsForUser(UUID userId) {
        return documentRepository.findByUserId(userId).stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    public List<DocumentResponse> getDocumentsForUserByMobile(String countryCode, String mobile) {
        User user = findUserByMobile(countryCode, mobile);
        return getDocumentsForUser(user.getId());
    }

    /**
     * Employer: same list of a specific worker's documents, so the employer
     * profile view shows the actual files the worker uploaded rather than
     * just an aggregate status.
     */
    public List<DocumentResponse> getDocumentsForWorker(UUID workerId) {
        return getDocumentsForUser(workerId);
    }

    /** Resolves a single document, enforcing that the requester may see it. */
    public Document getDocumentForAccess(UUID documentId, String countryCode, String requesterMobile, boolean requesterIsEmployer) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));

        if (requesterIsEmployer) {
            return document; // Employers may view any worker's documents to verify them.
        }

        User requester = findUserByMobile(countryCode, requesterMobile);
        if (!document.getUser().getId().equals(requester.getId())) {
            throw new SecurityException("Not authorized to access this document");
        }
        return document;
    }

    private DocumentResponse toDocumentResponse(Document document) {
        String url = fileStorageService.resolveUrl(document.getId(), document.getStorageKey());
        return new DocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getFileName(),
                url,
                document.getCreatedAt()
        );
    }

    private void updateVerificationStatusOnUpload(User user, DocumentType documentType) {
        // This logic maps a document upload to a verification type.
        VerificationType verificationTypeToUpdate = switch (documentType) {
            case AADHAAR, PAN, PASSPORT, DRIVING_LICENSE -> VerificationType.IDENTITY;
            case CERTIFICATION -> VerificationType.SKILL;
            default -> null;
        };

        if (verificationTypeToUpdate != null) {
            Verification verification = verificationRepository.findByUserId(user.getId()).stream()
                .filter(v -> v.getVerificationType() == verificationTypeToUpdate)
                .findFirst()
                .orElseGet(() -> {
                    Verification newVerification = new Verification();
                    newVerification.setUser(user);
                    newVerification.setVerificationType(verificationTypeToUpdate);
                    return newVerification;
                });

            if (verification.getStatus() == VerificationStatus.PENDING) {
                verification.setStatus(VerificationStatus.SUBMITTED);
                verificationRepository.save(verification);
            }
        }
    }

    public List<Verification> getPendingVerifications() {
        return verificationRepository.findByStatus(VerificationStatus.SUBMITTED);
    }

    public List<Verification> getVerificationsForWorker(UUID workerId) {
        return verificationRepository.findByUserId(workerId);
    }

    public EmployerVerification updateEmployerVerificationStatus(String countryCode, String employerMobile, UUID verificationId, VerificationStatus newStatus, String comments) {
        User employer = findUserByMobile(countryCode, employerMobile);

        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new IllegalArgumentException("Verification record not found"));

        EmployerVerification employerVerification = new EmployerVerification();
        employerVerification.setEmployer(employer);
        employerVerification.setVerification(verification);
        employerVerification.setStatus(newStatus);
        employerVerification.setReviewerComments(comments);
        return employerVerificationRepository.save(employerVerification);
    }
}
