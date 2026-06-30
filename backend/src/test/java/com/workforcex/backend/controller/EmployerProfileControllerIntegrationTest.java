package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployerProfileControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String MOBILE = "9000022222";

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
        String token = registerAndLoginAs(MOBILE, "EMPLOYER");

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
