package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CandidateSearchControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYER = "9000066661";
    private static final String WORKER1  = "9000066662"; // Mumbai, security, 5yrs, 15000
    private static final String WORKER2  = "9000066663"; // Pune, driving, 2yrs, 12000
    private static final String WORKER3  = "9000066664"; // Mumbai, security+driving, 8yrs, 25000

    private void setupWorkers() throws Exception {
        String w1Token = registerAndLoginAs(WORKER1, "WORKER");
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + w1Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Ramesh","skills":"security","experience":5,
                         "city":"Mumbai","preferredSalary":15000}
                        """));

        String w2Token = registerAndLoginAs(WORKER2, "WORKER");
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + w2Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Suresh","skills":"driving","experience":2,
                         "city":"Pune","preferredSalary":12000}
                        """));

        String w3Token = registerAndLoginAs(WORKER3, "WORKER");
        mockMvc.perform(put("/api/worker/profile")
                .header("Authorization", "Bearer " + w3Token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name":"Dinesh","skills":"security,driving","experience":8,
                         "city":"Mumbai","preferredSalary":25000}
                        """));
    }

    @Test
    void search_noFilters_returnsAllWorkersRanked() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    void search_filterByCity_returnsOnlyMumbaiWorkers() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Mumbai\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_filterBySkill_returnsOnlyMatchingWorkers() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":\"driving\"}"))
                .andExpect(status().isOk())
                // Worker2 (driving only) and Worker3 (security+driving) both match
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_filterByExperienceRange_returnsCorrectWorkers() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        // experienceMin=3 should return Worker1 (5yrs) and Worker3 (8yrs), not Worker2 (2yrs)
        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"experienceMin\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_filterBySalaryMax_excludesHighExpectationWorkers() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        // salaryMax=20000 excludes Worker3 (expects 25000)
        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"salaryMax\":20000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_multipleFilters_narrowsResults() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        // city=Mumbai + skills=security + experienceMin=3 → only Ramesh (Worker1) matches
        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"city\":\"Mumbai\",\"skills\":\"security\",\"experienceMin\":3,\"salaryMax\":20000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Ramesh"))
                .andExpect(jsonPath("$[0].skillScore").value(100.0))
                .andExpect(jsonPath("$[0].locationScore").value(100.0));
    }

    @Test
    void search_resultsIncludeScoreBreakdown() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        setupWorkers();

        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skills\":\"security\",\"city\":\"Mumbai\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalScore").isNumber())
                .andExpect(jsonPath("$[0].skillScore").isNumber())
                .andExpect(jsonPath("$[0].experienceScore").isNumber())
                .andExpect(jsonPath("$[0].locationScore").isNumber())
                .andExpect(jsonPath("$[0].salaryScore").isNumber());
    }

    @Test
    void search_workerCannotCallSearchEndpoint() throws Exception {
        String workerToken = registerAndLoginAs(WORKER1, "WORKER");
        mockMvc.perform(post("/api/matching/search")
                        .header("Authorization", "Bearer " + workerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}
