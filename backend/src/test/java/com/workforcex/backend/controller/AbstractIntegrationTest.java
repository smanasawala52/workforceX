package com.workforcex.backend.controller;

import com.workforcex.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Base class for all integration tests.
 * - ActiveProfiles("test") disables DataInitializer so 200 dummy jobs
 *   are NOT seeded during test runs, keeping tests fast and isolated.
 * - cleanAllTables() deletes in FK-safe order before every test.
 * - registerAndLoginAs() is a shared helper so each test class doesn't
 *   repeat the register+login boilerplate.
 */
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

    @BeforeEach
    void cleanAllTables() {
        applicationRepository.deleteAll(); // must be before jobs and users
        notificationRepository.deleteAll(); // must be before users
        jobRepository.deleteAll();
        workerProfileRepository.deleteAll();
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected String registerAndLoginAs(String mobileNumber, String role) throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "%s", "countryCode": "+91" }
                """.formatted(mobileNumber, role);
        mockMvc.perform(post("/api/auth/register")
                .contentType(APPLICATION_JSON)
                .content(registerBody));

        String loginBody = """
                { "mobileNumber": "%s", "password": "%s" }
                """.formatted(mobileNumber, mobileNumber);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return result.getResponse().getContentAsString()
                .split("\"token\":\"")[1].split("\"")[0];
    }
}
