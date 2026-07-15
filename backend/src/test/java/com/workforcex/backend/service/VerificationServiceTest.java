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
import com.workforcex.backend.service.storage.FileStorageService;
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
    @Mock private EmployerVerificationRepository employerVerificationRepository;
    @Mock private FileStorageService fileStorageService;

    @InjectMocks private VerificationService verificationService;

    private static final String COUNTRY_CODE = "+91";

    private User user;
    private User employer;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setCountryCode(COUNTRY_CODE);
        user.setMobileNumber("1234567890");

        employer = new User();
        employer.setId(UUID.randomUUID());
        employer.setCountryCode(COUNTRY_CODE);
        employer.setMobileNumber("9876543210");
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndSubmitForVerification() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf", "application/pdf", new byte[0]);
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, user.getMobileNumber())).thenReturn(Optional.of(user));
        when(verificationRepository.findByUserId(user.getId())).thenReturn(new ArrayList<>());
        when(documentRepository.save(any(Document.class))).thenAnswer(i -> i.getArguments()[0]);
        when(fileStorageService.store(any(), any())).thenReturn(user.getId() + "/aadhaar.pdf");

        Document doc = verificationService.uploadDocument(COUNTRY_CODE, user.getMobileNumber(), DocumentType.AADHAAR, file);

        verify(documentRepository).save(any(Document.class));
        assertThat(doc.getStorageKey()).isEqualTo(user.getId() + "/aadhaar.pdf");
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
        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, user.getMobileNumber())).thenReturn(Optional.of(user));
        when(verificationRepository.findByUserId(user.getId())).thenReturn(verifications);

        verificationService.uploadDocument(COUNTRY_CODE, user.getMobileNumber(), DocumentType.PAN, file);

        verify(verificationRepository).save(existingVerification);
        assertThat(existingVerification.getStatus()).isEqualTo(VerificationStatus.SUBMITTED);
    }

    @Test
    void updateEmployerVerificationStatus_shouldSaveNewEmployerVerification() {
        Verification verification = new Verification();
        verification.setId(UUID.randomUUID());

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, employer.getMobileNumber())).thenReturn(Optional.of(employer));
        when(verificationRepository.findById(verification.getId())).thenReturn(Optional.of(verification));
        when(employerVerificationRepository.save(any(EmployerVerification.class))).thenAnswer(i -> i.getArguments()[0]);

        EmployerVerification result = verificationService.updateEmployerVerificationStatus(
                COUNTRY_CODE, employer.getMobileNumber(), verification.getId(), VerificationStatus.VERIFIED, "Looks good.");

        verify(employerVerificationRepository).save(any(EmployerVerification.class));
        assertThat(result.getEmployer()).isEqualTo(employer);
        assertThat(result.getVerification()).isEqualTo(verification);
        assertThat(result.getStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(result.getReviewerComments()).isEqualTo("Looks good.");
    }
}
