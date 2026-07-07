package com.workforcex.backend.controller;

import com.workforcex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class AbstractIntegrationTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected UserRepository userRepository;
    @Autowired protected WorkerProfileRepository workerProfileRepository;
    @Autowired protected EmployerProfileRepository employerProfileRepository;
    @Autowired protected JobRepository jobRepository;
    @Autowired protected JobApplicationRepository applicationRepository;
    @Autowired protected NotificationRepository notificationRepository;
    @Autowired protected DocumentRepository documentRepository;
    @Autowired protected VerificationRepository verificationRepository;

    @BeforeEach
    void cleanAllTables() {
        applicationRepository.deleteAll();
        notificationRepository.deleteAll();
        documentRepository.deleteAll();
        verificationRepository.deleteAll();
        jobRepository.deleteAll();
        workerProfileRepository.deleteAll();
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String registerAndLoginAs(String mobileNumber, String role) throws Exception {
        String registerBody = String.format(
            "{ \"mobileNumber\": \"%s\", \"password\": \"%s\", \"role\": \"%s\" }",
            mobileNumber, mobileNumber, role
        );

        // Allow registration to fail if user already exists (e.g. from previous test)
        mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content(registerBody));

        String loginBody = String.format(
            "{ \"mobileNumber\": \"%s\", \"password\": \"%s\" }",
            mobileNumber, mobileNumber
        );
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString()
                .split("\"token\":\"")[1].split("\"")[0];
    }
}
