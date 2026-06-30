package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JobControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYER_A = "9000033331";
    private static final String EMPLOYER_B = "9000033332";
    private static final String WORKER     = "9000033333";

    private String jobBody() {
        return """
                {
                  "title": "Security Guard",
                  "skillsRequired": "security,patrolling",
                  "experienceRequired": 2,
                  "location": "Ahmedabad",
                  "salary": 15000,
                  "description": "Night shift security guard"
                }
                """;
    }

    @Test
    void employer_canCreateEditViewDeleteOwnJob() throws Exception {
        String token = registerAndLoginAs(EMPLOYER_A, "EMPLOYER");

        MvcResult createResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Security Guard"))
                .andReturn();

        String jobId = createResult.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("Ahmedabad"));

        mockMvc.perform(get("/api/jobs")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(put("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Senior Security Guard",
                                  "skillsRequired": "security,patrolling",
                                  "experienceRequired": 5,
                                  "location": "Ahmedabad",
                                  "salary": 20000,
                                  "description": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Senior Security Guard"));

        mockMvc.perform(delete("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void worker_cannotCreateJob() throws Exception {
        String workerToken = registerAndLoginAs(WORKER, "WORKER");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void employer_cannotAccessAnotherEmployersJob() throws Exception {
        String tokenA = registerAndLoginAs(EMPLOYER_A, "EMPLOYER");
        String tokenB = registerAndLoginAs(EMPLOYER_B, "EMPLOYER");

        MvcResult createResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jobBody()))
                .andExpect(status().isCreated())
                .andReturn();

        String jobId = createResult.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        mockMvc.perform(get("/api/jobs/" + jobId)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You do not have permission to access this job"));
    }
}
