package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MatchingControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYER = "9000055551";
    private static final String WORKER1  = "9000055552"; // perfect match
    private static final String WORKER2  = "9000055553"; // partial match
    private static final String WORKER3  = "9000055554"; // poor match

    @Test
    void matching_returnsRankedWorkers_highestScoreFirst() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String worker1Token  = registerAndLoginAs(WORKER1, "WORKER");
        String worker2Token  = registerAndLoginAs(WORKER2, "WORKER");
        String worker3Token  = registerAndLoginAs(WORKER3, "WORKER");

        // Worker 1: perfect match — all skills, enough experience, same city, right salary
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + worker1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Ramesh","skills":"security,patrolling","experience":5,"city":"Ahmedabad","preferredSalary":14000}
                        """));

        // Worker 2: partial match — has one skill, less experience, different city
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + worker2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Suresh","skills":"security","experience":1,"city":"Surat","preferredSalary":14000}
                        """));

        // Worker 3: poor match — different skills, no experience, different city, high salary expectation
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + worker3Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Dinesh","skills":"cooking","experience":0,"city":"Mumbai","preferredSalary":30000}
                        """));

        // Create a job
        MvcResult jobResult = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Security Guard","companyName":"Test Company 5","skillsRequired":"security,patrolling","experienceRequired":3,"location":"Ahmedabad,Surat","salaryMin":14000,"salaryMax":16000,"openPositions":3}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String jobId = jobResult.getResponse().getContentAsString().split("\"id\":\"")[1].split("\"")[0];

        // Run matching
        mockMvc.perform(get("/api/matching/" + jobId)
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                // Worker 1 must be ranked first (highest score)
                .andExpect(jsonPath("$[0].name").value("Ramesh"))
                // Scores must be in descending order
                .andExpect(jsonPath("$[0].score").value(org.hamcrest.Matchers.greaterThan(
                        (Double) null != null ? 0.0 : 0.0)))
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    // Parse scores manually and verify order
                    String[] scores = body.split("\"score\":") ;
                    double score0 = Double.parseDouble(scores[1].split("[,}]")[0]);
                    double score1 = Double.parseDouble(scores[2].split("[,}]")[0]);
                    double score2 = Double.parseDouble(scores[3].split("[,}]")[0]);
                    org.assertj.core.api.Assertions.assertThat(score0).isGreaterThanOrEqualTo(score1);
                    org.assertj.core.api.Assertions.assertThat(score1).isGreaterThanOrEqualTo(score2);
                });
    }

    @Test
    void matching_workerCannotCallMatchingEndpoint() throws Exception {
        String workerToken = registerAndLoginAs(WORKER1, "WORKER");
        mockMvc.perform(get("/api/matching/00000000-0000-0000-0000-000000000000")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isForbidden());
    }
}
