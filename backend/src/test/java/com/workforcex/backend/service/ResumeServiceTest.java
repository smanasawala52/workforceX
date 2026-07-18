package com.workforcex.backend.service;

import com.workforcex.backend.dto.ResumeParseResponse;
import com.workforcex.backend.entity.Skill;
import com.workforcex.backend.entity.User;
import com.workforcex.backend.entity.WorkerProfile;
import com.workforcex.backend.repository.SkillRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkerProfileRepository workerProfileRepository;
    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private ResumeService resumeService;

    private static final String COUNTRY_CODE = "+91";
    private static final String MOBILE = "9876543210";
    private static final String FULL_MOBILE = COUNTRY_CODE + MOBILE;

    private User user() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setCountryCode(COUNTRY_CODE);
        u.setMobileNumber(MOBILE);
        return u;
    }

    private byte[] minimalPdfWithText(String text) {
        String pdf = "%PDF-1.4\n" +
                "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n" +
                "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n" +
                "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792]\n" +
                "/Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n" +
                "4 0 obj\n<< /Length " + (text.length() + 50) + " >>\nstream\n" +
                "BT /F1 12 Tf 50 700 Td (" + text + ") Tj ET\nendstream\nendobj\n" +
                "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n" +
                "xref\n0 6\n0000000000 65535 f\n" +
                "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n0\n%%EOF";
        return pdf.getBytes();
    }

    @Test
    void parseAndSave_throws_whenFileIsEmpty() {
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> resumeService.parseAndSave(FULL_MOBILE, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Uploaded file is empty");
    }

    @Test
    void parseAndSave_throws_whenContentTypeIsNotPdf() {
        MultipartFile file = new MockMultipartFile(
                "file", "resume.docx", "application/msword", new byte[]{1, 2, 3});

        assertThatThrownBy(() -> resumeService.parseAndSave(FULL_MOBILE, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PDF files are accepted");
    }

    @Test
    void parseAndSave_throws_whenContentTypeIsNull() {
        MultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", null, new byte[]{1, 2, 3});

        assertThatThrownBy(() -> resumeService.parseAndSave(FULL_MOBILE, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Only PDF files are accepted");
    }

    @Test
    void parseAndSave_throws_whenPdfIsMalformed() {
        MultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", "not a real pdf".getBytes());

        assertThatThrownBy(() -> resumeService.parseAndSave(FULL_MOBILE, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Could not parse PDF file");
    }

    @Test
    void parseAndSave_createsNewProfile_detectsSkillsAndExperience() throws Exception {
        User user = user();
        byte[] pdfBytes = minimalPdfWithText(
                "Security Guard with 5 years experience in patrolling and surveillance. Skills: security, cctv, driving.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeParseResponse response = resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(response.fileName()).isEqualTo("resume.pdf");
        assertThat(response.detectedSkillList()).isNotEmpty();
        assertThat(response.detectedSkillList()).contains("security", "cctv", "driving", "patrolling", "surveillance");
        assertThat(response.detectedExperience()).isEqualTo(5);
        assertThat(response.message()).contains("skill(s) detected");

        verify(workerProfileRepository).save(argThat(p ->
                p.getResumeFileName().equals("resume.pdf")
                        && p.getSkill1() != null
                        && p.getExperience().equals(5)));
    }

    @Test
    void parseAndSave_doesNotOverwriteExistingSkills_onExistingProfile() throws Exception {
        User user = user();
        WorkerProfile existing = new WorkerProfile();
        existing.setUserId(user.getId());
        existing.setSkill1("cooking");
        existing.setExperience(10);

        byte[] pdfBytes = minimalPdfWithText("Security guard, 5 years experience in patrolling.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(existing.getSkill1()).isEqualTo("cooking"); // untouched, already set
        assertThat(existing.getExperience()).isEqualTo(10); // untouched, already set
        verify(skillRepository, never()).findAll();
    }

    @Test
    void parseAndSave_returnsNoSkillsMessage_whenNoKeywordsMatch() throws Exception {
        User user = user();
        byte[] pdfBytes = minimalPdfWithText("Lorem ipsum dolor sit amet consectetur adipiscing elit.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeParseResponse response = resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(response.detectedSkillList()).isEmpty();
        assertThat(response.detectedExperience()).isNull();
        assertThat(response.message()).contains("No matching skills detected");
    }

    @Test
    void parseAndSave_truncatesLongResumeTextAndPreview() throws Exception {
        User user = user();
        String longText = "security ".repeat(1200); // > 9000 chars
        byte[] pdfBytes = minimalPdfWithText(longText);
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeParseResponse response = resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(response.rawTextPreview()).endsWith("...");
        verify(workerProfileRepository).save(argThat(p -> p.getResumeText().length() <= 9000));
    }

    @Test
    void parseAndSave_seedsOnlyNewSkills() throws Exception {
        User user = user();
        Skill existingSkill = new Skill();
        existingSkill.setName("security");

        byte[] pdfBytes = minimalPdfWithText("Skills: security, cctv.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of(existingSkill));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        resumeService.parseAndSave(FULL_MOBILE, file);

        ArgumentCaptor<List<Skill>> captor = ArgumentCaptor.forClass(List.class);
        verify(skillRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Skill::getName).doesNotContain("security");
    }

    @Test
    void parseAndSave_seedSkillsSwallowsRepositoryException() throws Exception {
        User user = user();
        byte[] pdfBytes = minimalPdfWithText("Skills: security, cctv.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenThrow(new RuntimeException("db down"));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeParseResponse response = resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(response.detectedSkillList()).isNotEmpty();
    }

    @Test
    void parseAndSave_doesNotOverwriteExperience_whenAlreadySet_evenIfNothingDetected() throws Exception {
        User user = user();
        WorkerProfile existing = new WorkerProfile();
        existing.setUserId(user.getId());
        existing.setExperience(7);
        existing.setSkill1("cooking");

        byte[] pdfBytes = minimalPdfWithText("No relevant keywords here at all.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        resumeService.parseAndSave(FULL_MOBILE, file);

        assertThat(existing.getExperience()).isEqualTo(7);
    }

    @Test
    void parseAndSave_recognizesUaeMobileNumber() throws Exception {
        User user = user();
        user.setCountryCode("+971");
        byte[] pdfBytes = minimalPdfWithText("Skills: cooking.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber("+971", "501234567")).thenReturn(Optional.of(user));
        when(workerProfileRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(skillRepository.findAll()).thenReturn(List.of());
        when(workerProfileRepository.save(any(WorkerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        ResumeParseResponse response = resumeService.parseAndSave("+971501234567", file);

        assertThat(response.fileName()).isEqualTo("resume.pdf");
    }

    @Test
    void parseAndSave_throws_whenUserNotFound() {
        byte[] pdfBytes = minimalPdfWithText("Skills: cooking.");
        MultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", pdfBytes);

        when(userRepository.findByCountryCodeAndMobileNumber(COUNTRY_CODE, MOBILE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resumeService.parseAndSave(FULL_MOBILE, file))
                .isInstanceOf(ResponseStatusException.class);
    }
}
