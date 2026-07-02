package com.workforcex.worker.api;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface WorkforceXApi {

    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @PUT("api/worker/profile")
    Call<WorkerProfileResponse> saveWorkerProfile(
            @Header("Authorization") String token,
            @Body WorkerProfileRequest request);

    @GET("api/worker/profile")
    Call<WorkerProfileResponse> getWorkerProfile(@Header("Authorization") String token);

    // Spiral 2: browse all available jobs
    @GET("api/jobs/browse")
    Call<List<JobBrowseItem>> browseJobs(@Header("Authorization") String token);
}
