package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class JobBrowseControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYER = "9000077771";
    private static final String WORKER   = "9000077772";

    @Test
    void worker_canBrowseAllJobs() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");

        // Employer posts two jobs
        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + employerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Security Guard","companyName":"Test Company 2","skillsRequired":"security","experienceRequired":2,
                     "location":"Mumbai","salaryMin":14000,"salaryMax":16000,"openPositions":3,
                     "description":"Night shift"}
                    """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + employerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Driver","companyName":"Test Company 3","skillsRequired":"driving","experienceRequired":1,
                     "location":"Pune","salaryMin":12000,"salaryMax":15000,"openPositions":2,
                     "description":"Day shift delivery"}
                    """))
                .andExpect(status().isCreated());

        // Worker browses jobs and sees both
        String workerToken = registerAndLoginAs(WORKER, "WORKER");

        mockMvc.perform(get("/api/jobs/browse")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").isString())
                .andExpect(jsonPath("$[0].companyName").isString())
                .andExpect(jsonPath("$[0].salaryMin").isNumber())
                .andExpect(jsonPath("$[0].salaryMax").isNumber())
                .andExpect(jsonPath("$[0].openPositions").isNumber());
    }

    @Test
    void employer_cannotCallBrowseEndpoint() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        mockMvc.perform(get("/api/jobs/browse")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void worker_seesCompanyName_whenEmployerFilledProfile() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");

        // Employer fills in their profile with a company name
        mockMvc.perform(put("/api/employer/profile")
                .header("Authorization", "Bearer " + employerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"companyName":"Acme Security Ltd","contactPerson":"Priya Shah"}
                    """));

        mockMvc.perform(post("/api/jobs")
                .header("Authorization", "Bearer " + employerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Guard","skillsRequired":"security","salaryMin":14000,"salaryMax":16000,"openPositions":1,"companyName":"Acme Security Ltd"}
                    """))
                .andExpect(status().isCreated());

        String workerToken = registerAndLoginAs(WORKER, "WORKER");

        mockMvc.perform(get("/api/jobs/browse")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Acme Security Ltd"));
    }
}
