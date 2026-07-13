package com.workforcex.backend.controller;

import com.workforcex.backend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VerificationControllerIntegrationTest extends AbstractIntegrationTest {

    private String workerToken;
    private String employerToken;
    private User worker;

    @BeforeEach
    void setUp() throws Exception {
        workerToken = registerAndLoginAs("9876543210", "WORKER");
        employerToken = registerAndLoginAs("1112223334", "EMPLOYER");
        worker = userRepository.findByMobileNumber("9876543210").get();
    }

    @Test
    void worker_canUploadDocumentAndItCreatesVerificationRecord() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf", MediaType.APPLICATION_PDF_VALUE, "fake-pdf-content".getBytes());

        mockMvc.perform(multipart("/api/verification/upload")
                        .file(file)
                        .param("type", "AADHAAR")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/verification/status")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].verificationType").value("IDENTITY"))
                .andExpect(jsonPath("$[0].status").value("SUBMITTED"));
    }

    @Test
    void worker_andEmployer_seeTheSameUploadedDocuments() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "aadhaar.pdf", MediaType.APPLICATION_PDF_VALUE, "fake-pdf-content".getBytes());

        mockMvc.perform(multipart("/api/verification/upload")
                        .file(file)
                        .param("type", "AADHAAR")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk());

        // Worker sees the document they uploaded.
        mockMvc.perform(get("/api/verification/documents")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentType").value("AADHAAR"))
                .andExpect(jsonPath("$[0].fileName").value("aadhaar.pdf"))
                .andExpect(jsonPath("$[0].fileUrl").exists());

        // The same document is visible to an employer viewing this worker's profile.
        mockMvc.perform(get("/api/verification/worker/" + worker.getId() + "/documents")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].documentType").value("AADHAAR"))
                .andExpect(jsonPath("$[0].fileName").value("aadhaar.pdf"))
                .andExpect(jsonPath("$[0].fileUrl").exists());
    }

    @Test
    void employer_canUpdateVerificationStatus() throws Exception {
        worker_canUploadDocumentAndItCreatesVerificationRecord();

        MvcResult result = mockMvc.perform(get("/api/verification/status")
                        .header("Authorization", "Bearer " + workerToken))
                .andReturn();
        String verificationId = result.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        String comments = "Aadhaar number verified.";

        mockMvc.perform(put("/api/verification/" + verificationId + "/status")
                        .header("Authorization", "Bearer " + employerToken)
                        .param("status", "VERIFIED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( comments))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("VERIFIED"))
                .andExpect(jsonPath("$.reviewerComments").value(comments));
    }
}
