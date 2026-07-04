package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ResumeControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String WORKER   = "9000088881";
    private static final String EMPLOYER = "9000088882";

    @Test
    void worker_canUploadResumePdf_andReceiveParsedResult() throws Exception {
        String workerToken = registerAndLoginAs(WORKER, "WORKER");

        // Create a minimal PDF-like content with skills keywords
        // Real PDFBox test needs a valid PDF — we simulate with a text-based PDF snippet
        byte[] pdfContent = createMinimalPdfWithText(
                "Security Guard with 5 years experience in patrolling and surveillance. " +
                "Skills: security, cctv, driving. Worked as supervisor for 3 years."
        );

        MockMultipartFile resumeFile = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", pdfContent);

        mockMvc.perform(multipart("/api/worker/resume")
                        .file(resumeFile)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("resume.pdf"))
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void employer_cannotUploadResume() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");

        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/worker/resume")
                        .file(file)
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_rejectsNonPdfFile() throws Exception {
        String workerToken = registerAndLoginAs(WORKER, "WORKER");

        MockMultipartFile wordFile = new MockMultipartFile(
                "file", "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/worker/resume")
                        .file(wordFile)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isBadRequest());
    }

    /**
     * Creates a minimal valid PDF with given text content.
     * This is a bare-bones PDF structure that PDFBox can parse.
     */
    private byte[] createMinimalPdfWithText(String text) {
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
}
