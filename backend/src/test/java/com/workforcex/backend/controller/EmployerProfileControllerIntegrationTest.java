package com.workforcex.backend.controller;

import com.workforcex.backend.repository.EmployerProfileRepository;
import com.workforcex.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployerProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployerProfileRepository employerProfileRepository;

    private static final String MOBILE = "9000022222";

    @BeforeEach
    void cleanDatabase() {
        employerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndLogin() throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "EMPLOYER" }
                """.formatted(MOBILE);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody));

        String loginBody = """
                { "mobileNumber": "%s", "password": "%s" }
                """.formatted(MOBILE, MOBILE);
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        return loginResult.getResponse().getContentAsString().split("\"token\":\"")[1].split("\"")[0];
    }

    @Test
    void getProfile_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/employer/profile"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    org.assertj.core.api.Assertions.assertThat(status).isIn(401, 403);
                });
    }

    @Test
    void saveThenGetProfile_succeeds_withValidToken() throws Exception {
        String token = registerAndLogin();

        String profileBody = """
                {
                  "companyName": "Acme Facility Services",
                  "contactPerson": "Priya Shah",
                  "email": "priya@acme.example",
                  "address": "SG Highway, Ahmedabad"
                }
                """;

        mockMvc.perform(put("/api/employer/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Acme Facility Services"))
                .andExpect(jsonPath("$.mobileNumber").value(MOBILE));

        mockMvc.perform(get("/api/employer/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contactPerson").value("Priya Shah"));
    }
}
