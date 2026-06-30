package com.workforcex.backend.controller;

import com.workforcex.backend.repository.UserRepository;
import com.workforcex.backend.repository.WorkerProfileRepository;
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
class WorkerProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkerProfileRepository workerProfileRepository;

    private static final String MOBILE = "9000011111";

    @BeforeEach
    void cleanDatabase() {
        workerProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndLogin() throws Exception {
        String registerBody = """
                { "mobileNumber": "%s", "role": "WORKER" }
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

        String responseBody = loginResult.getResponse().getContentAsString();
        // crude extraction - good enough for a test, avoids pulling in extra parsing deps
        return responseBody.split("\"token\":\"")[1].split("\"")[0];
    }

    @Test
    void getProfile_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/worker/profile"))
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
                  "name": "Ramesh Kumar",
                  "gender": "Male",
                  "city": "Ahmedabad",
                  "state": "Gujarat",
                  "skills": "driving,security",
                  "experience": 5,
                  "preferredSalary": 18000
                }
                """;

        mockMvc.perform(put("/api/worker/profile")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(profileBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.city").value("Ahmedabad"))
                .andExpect(jsonPath("$.mobileNumber").value(MOBILE));

        mockMvc.perform(get("/api/worker/profile")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ramesh Kumar"))
                .andExpect(jsonPath("$.skills").value("driving,security"))
                .andExpect(jsonPath("$.experience").value(5));
    }
}
