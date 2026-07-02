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
            @Header("Authorization") String token,
            @Body EmployerProfileRequest request);

    @GET("api/employer/profile")
    Call<EmployerProfileResponse> getEmployerProfile(@Header("Authorization") String token);

    @POST("api/jobs")
    Call<JobResponse> createJob(@Header("Authorization") String token, @Body JobRequest request);

    @GET("api/jobs")
    Call<List<JobResponse>> getMyJobs(@Header("Authorization") String token);

    @GET("api/jobs/{jobId}")
    Call<JobResponse> getJobById(@Header("Authorization") String token, @Path("jobId") String jobId);

    @PUT("api/jobs/{jobId}")
    Call<JobResponse> updateJob(@Header("Authorization") String token, @Path("jobId") String jobId, @Body JobRequest request);

    @DELETE("api/jobs/{jobId}")
    Call<Void> deleteJob(@Header("Authorization") String token, @Path("jobId") String jobId);

    @GET("api/matching/{jobId}")
    Call<List<MatchedWorker>> getMatchedWorkers(@Header("Authorization") String token, @Path("jobId") String jobId);

    // ── Spiral 2: candidate search ────────────────────────────────────────────

    @GET("api/matching/search")
    Call<List<CandidateSearchResult>> searchCandidates(
            @Header("Authorization") String token,
            @Query("skills") String skills,
            @Query("city") String city,
            @Query("experienceMin") Integer experienceMin,
            @Query("experienceMax") Integer experienceMax,
            @Query("salaryMin") Double salaryMin,
            @Query("salaryMax") Double salaryMax
    );
}
