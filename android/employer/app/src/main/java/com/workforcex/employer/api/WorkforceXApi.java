package com.workforcex.employer.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface WorkforceXApi {

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @PUT("api/employer/profile")
    Call<EmployerProfileResponse> saveEmployerProfile(
            @Header("Authorization") String token, @Body EmployerProfileRequest request);

    @GET("api/employer/profile")
    Call<EmployerProfileResponse> getEmployerProfile(@Header("Authorization") String token);

    @POST("api/jobs")
    Call<JobResponse> createJob(@Header("Authorization") String token, @Body JobRequest request);

    @GET("api/jobs")
    Call<List<JobResponse>> getMyJobs(@Header("Authorization") String token);

    @GET("api/jobs/{jobId}")
    Call<JobResponse> getJobById(@Header("Authorization") String token, @Path("jobId") String jobId);

    @PUT("api/jobs/{jobId}")
    Call<JobResponse> updateJob(@Header("Authorization") String token,
            @Path("jobId") String jobId, @Body JobRequest request);

    @DELETE("api/jobs/{jobId}")
    Call<Void> deleteJob(@Header("Authorization") String token, @Path("jobId") String jobId);

    @GET("api/matching/{jobId}")
    Call<List<MatchedWorker>> getMatchedWorkers(@Header("Authorization") String token,
            @Path("jobId") String jobId);

    @POST("api/matching/search") // Changed to POST
    Call<List<CandidateSearchResult>> searchCandidates(
            @Header("Authorization") String token,
            @Body CandidateSearchRequest request); // Changed to @Body

    // Applications
    @GET("api/applications/job/{jobId}")
    Call<List<JobApplicationItem>> getApplicationsForJob(
            @Header("Authorization") String token, @Path("jobId") String jobId);

    @PUT("api/applications/{applicationId}/status")
    Call<JobApplicationItem> updateApplicationStatus(
            @Header("Authorization") String token,
            @Path("applicationId") String applicationId,
            @Query("status") String status);

    @POST("api/applications/offer")
    Call<JobApplicationItem> offerJob(
            @Header("Authorization") String token,
            @Query("jobId") String jobId,
            @Query("workerId") String workerId);

    // Notifications
    @GET("api/notifications")
    Call<List<Notification>> getUnreadNotifications(@Header("Authorization") String token);

    @PUT("api/notifications/{notificationId}/read")
    Call<Void> markAsRead(@Header("Authorization") String token, @Path("notificationId") String notificationId);

    // Verification
    @GET("api/verification/pending")
    Call<List<Verification>> getPendingVerifications(@Header("Authorization") String token);

    @GET("api/verification/worker/{workerId}")
    Call<List<Verification>> getWorkerDocuments(@Header("Authorization") String token, @Path("workerId") String workerId);

    @PUT("api/verification/{verificationId}/status")
    Call<Void> updateEmployerVerificationStatus(
            @Header("Authorization") String token,
            @Path("verificationId") String verificationId,
            @Query("status") String status,
            @Body String comments);

    // Skills
    @GET("api/skills")
    Call<List<Skill>> getSkills();
}
