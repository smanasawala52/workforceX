package com.workforcex.backend.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final UserRepository userRepository;
    private final DocumentRepository documentRepository;
    private final VerificationRepository verificationRepository;
    private final EmployerVerificationRepository employerVerificationRepository;
    // In a real app, this would be a service that uploads to S3, Google Cloud Storage, etc.
    // private final FileStorageService fileStorageService;

    public List<Verification> getVerificationStatusForUser(String userMobile) {
        User user = userRepository.findByMobileNumber(userMobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return verificationRepository.findByUserId(user.getId());
    }

    public Document uploadDocument(String userMobile, DocumentType documentType, MultipartFile file) {
        User user = userRepository.findByMobileNumber(userMobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Simulate file upload and get a URL
        // String fileUrl = fileStorageService.upload(file);
        String fileUrl = "/uploads/" + user.getId() + "/" + file.getOriginalFilename();

        Document document = new Document();
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setFileName(file.getOriginalFilename());
        document.setFileUrl(fileUrl);
        documentRepository.save(document);

        // When a document is uploaded, update the corresponding verification status to "SUBMITTED"
        updateVerificationStatusOnUpload(user, documentType);

        return document;
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

    public EmployerVerification updateEmployerVerificationStatus(String employerMobile, UUID verificationId, VerificationStatus newStatus, String comments) {
        User employer = userRepository.findByMobileNumber(employerMobile)
                .orElseThrow(() -> new IllegalArgumentException("Employer not found"));

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
