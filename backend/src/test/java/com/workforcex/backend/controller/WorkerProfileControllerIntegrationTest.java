package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WorkerProfileControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String MOBILE = "9000011111";

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
        String token = registerAndLoginAs(MOBILE, "WORKER");

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
