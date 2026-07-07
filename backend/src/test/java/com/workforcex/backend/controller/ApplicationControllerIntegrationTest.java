package com.workforcex.backend.controller;

import com.workforcex.backend.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
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
                .andExpect(status().isCreated());
    }

    @Test
    void worker_cannotApplyTwice() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId).header("Authorization", "Bearer " + workerToken));

        mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void worker_canViewMyApplications() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId).header("Authorization", "Bearer " + workerToken));

        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void employer_canViewApplicantsForJob() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String w1Token       = registerAndLoginAs(WORKER1, "WORKER");
        String w2Token       = registerAndLoginAs(WORKER2, "WORKER");
        String jobId         = createJob(employerToken);

        mockMvc.perform(post("/api/applications/" + jobId).header("Authorization", "Bearer " + w1Token));
        mockMvc.perform(post("/api/applications/" + jobId).header("Authorization", "Bearer " + w2Token));

        mockMvc.perform(get("/api/applications/job/" + jobId)
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void employer_canUpdateApplicationStatus() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken   = registerAndLoginAs(WORKER1, "WORKER");
        String jobId         = createJob(employerToken);

        MvcResult applyResult = mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andReturn();
        String applicationId = applyResult.getResponse().getContentAsString().split("\"applicationId\":\"")[1].split("\"")[0];

        mockMvc.perform(put("/api/applications/" + applicationId + "/status?status=SHORTLISTED")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHORTLISTED"));
    }

    @Test
    void employer_canOfferJobToWorker() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        registerAndLoginAs(WORKER1, "WORKER");
        String jobId = createJob(employerToken);
        User worker = userRepository.findByMobileNumber(WORKER1).get();

        mockMvc.perform(post("/api/applications/offer")
                        .header("Authorization", "Bearer " + employerToken)
                        .param("jobId", jobId)
                        .param("workerId", worker.getId().toString()))
                .andExpect(status().isCreated());
    }

    @Test
    void employer_cannotOfferJob_ifApplicationExists() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken = registerAndLoginAs(WORKER1, "WORKER");
        String jobId = createJob(employerToken);
        User worker = userRepository.findByMobileNumber(WORKER1).get();

        mockMvc.perform(post("/api/applications/" + jobId).header("Authorization", "Bearer " + workerToken));

        mockMvc.perform(post("/api/applications/offer")
                        .header("Authorization", "Bearer " + employerToken)
                        .param("jobId", jobId)
                        .param("workerId", worker.getId().toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void worker_receivesNotification_withCorrectLink_onStatusUpdate() throws Exception {
        String employerToken = registerAndLoginAs(EMPLOYER, "EMPLOYER");
        String workerToken = registerAndLoginAs(WORKER1, "WORKER");
        String jobId = createJob(employerToken);

        MvcResult applyResult = mockMvc.perform(post("/api/applications/" + jobId)
                        .header("Authorization", "Bearer " + workerToken))
                .andReturn();
        String applicationId = applyResult.getResponse().getContentAsString().split("\"applicationId\":\"")[1].split("\"")[0];

        mockMvc.perform(put("/api/applications/" + applicationId + "/status?status=SHORTLISTED")
                        .header("Authorization", "Bearer " + employerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/notifications").header("Authorization", "Bearer " + workerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].linkType").value("MY_APPLICATIONS"))
                .andExpect(jsonPath("$[0].linkId").value(applicationId));
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
