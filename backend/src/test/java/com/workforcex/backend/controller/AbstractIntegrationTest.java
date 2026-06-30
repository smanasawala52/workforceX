package com.workforcex.backend.controller;

import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.JobRepository;
import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * All integration test classes share the SAME H2 database within a single
 * Maven test run (Spring caches the application context). That means any
 * test class deleting `users` must first delete every table with a foreign
 * key pointing at users - regardless of which entity that test class is
 * directly testing.
 *
 * Centralizing cleanup here means adding a new FK-related table later
 * (e.g. matching results) only requires updating this ONE method.
 */
@SpringBootTest
@AutoConfigureMockMvc
abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected WorkerProfileRepository workerProfileRepository;

    @Autowired
    protected EmployerProfileRepository employerProfileRepository;

    @Autowired
    protected JobRepository jobRepository;

    @BeforeEach
    void cleanAllTables() {
        jobRepository.deleteAll();
        workerProfileRepository.deleteAll();
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    /**
     * Registers a user with the given mobile/role, logs in, and returns the JWT.
     * Dev-mode rule: password always equals mobile number.
     */
    protected String registerAndLoginAs(String mobileNumber, String role) throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "%s" }
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

        return result.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
    }
}
