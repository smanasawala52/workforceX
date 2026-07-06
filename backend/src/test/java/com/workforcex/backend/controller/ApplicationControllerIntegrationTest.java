package com.workforcex.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ApplicationControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String EMPLOYER = "9000099991";
    private static final String WORKER1  = "9000099992";
    private static final String WORKER2  = "9000099993";

    private String createJob(String employerToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + employerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"title":"Security Guard","skillsRequired":"security",
                             "location":"Mumbai","salaryMin":14000,"salaryMax":16000,"openPositions":3}
                            """))
                .andExpect(status().isCreated())
                .andReturn();
        return result.getResponse().getContentAsString()
                .split("\"id\":\"")[1].split("\"")[0];
    }

    @Test
    void worker_canApplyToJob() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void worker_cannotApplyTwice() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + workerToken));

        mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("You have already applied to this job"));
    }

    @Test
    void worker_canViewMyApplications() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + workerToken));

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].jobTitle").value("Security Guard"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void employer_canViewApplicantsForJob() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String w1Token       = registerAndLoginAs(WORKER1, "WORKER");
        String w2Token       = registerAndLoginAs(WORKER2, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + w1Token));
        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + w2Token));

        mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void employer_canShortlistApplicant() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + workerToken));

        MvcResult appsResult = mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", "Bearer " + employerToken))
                .andReturn();
        String applicationId = appsResult.getResponse().getContentAsString()
                .split("\"applicationId\":\"")[1].split("\"")[0];

        mockMvc.perform(put("/api/applications/" + applicationId + "/status?status=SHORTLISTED")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void employer_canRejectApplicant() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId)
                .header("Authorization", "Bearer " + workerToken));

        MvcResult appsResult = mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", "Bearer " + employerToken))
                .andReturn();
        String applicationId = appsResult.getResponse().getContentAsString()
                .split("\"applicationId\":\"")[1].split("\"")[0];

        mockMvc.perform(put("/api/applications/" + applicationId + "/status?status=REJECTED")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void employer_cannotViewAnotherEmployersApplications() throws Exception {
        String emp1Token = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String emp2Token = registerAndLoginAs("9000099994", "EMPLOYER");
        String jobId     = createJob(emp1Token);

        mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", "Bearer " + emp2Token))
                .andExpect(status().isBadRequest());
    }
}
