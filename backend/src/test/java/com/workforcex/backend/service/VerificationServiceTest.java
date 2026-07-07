package com.workforcex.backend.service;

import com.workforcex.backend.entity.Document;
import com.workforcex.backend.entity.DocumentType;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.Verification;
import com.workforcex.backend.entity.VerificationStatus;
import com.workforcex.backend.entity.VerificationType;
import com.workforcex.backend.repository.DocumentRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private VerificationRepository verificationRepository;

    @InjectMocks private VerificationService verificationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setMobileNumber("1234567890");
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndSubmitForVerification() {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf", "application/pdf", new byte[0]);
        when(userRepository.findByMobileNumber(user.getMobileNumber())).thenReturn(Optional.of(user));
        when(verificationRepository.findByUserId(user.getId())).thenReturn(new ArrayList<>());
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);

        Document doc = verificationService.uploadDocument(user.getMobileNumber(), DocumentType.AADHAAR, file);

        verify(documentRepository).save(any(Document.class));
        assertThat(doc.getFileUrl()).isEqualTo("/uploads/" + user.getId() + "/aadhaar.pdf");
        verify(verificationRepository).save(any(Verification.class));
    }

    @Test
    void updateVerificationStatusOnUpload_shouldUpdateExistingPendingVerification() {
        Verification existingVerification = new Verification();
        existingVerification.setUser(user);
        existingVerification.setVerificationType(VerificationType.IDENTITY);
        existingVerification.setStatus(VerificationStatus.PENDING);
        List<Verification> verifications = new ArrayList<>();
        verifications.add(existingVerification);

        MockMultipartFile file = new MockMultipartFile("file", "pan.pdf", "application/pdf", new byte[0]);
        when(userRepository.findByMobileNumber(user.getMobileNumber())).thenReturn(Optional.of(user));
        when(verificationRepository.findByUserId(user.getId())).thenReturn(verifications);

        verificationService.uploadDocument(user.getMobileNumber(), DocumentType.PAN, file);

        verify(verificationRepository).save(existingVerification);
        assertThat(existingVerification.getStatus()).isEqualTo(VerificationStatus.SUBMITTED);
    }

    @Test
    void updateVerificationStatus_shouldUpdateStatusAndComments() {
        Verification verification = new Verification();
        verification.setId(UUID.randomUUID());
        when(verificationRepository.findById(verification.getId())).thenReturn(Optional.of(verification));
        when(verificationRepository.save(any(Verification.class))).thenAnswer(i -> i.getArguments()[0]);

        Verification updatedVerification = verificationService.updateVerificationStatus(verification.getId(), VerificationStatus.VERIFIED, "Looks good.");

        verify(verificationRepository).save(verification);
        assertThat(updatedVerification.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(updatedVerification.getReviewerComments()).isEqualTo("Looks good.");
    }
}
