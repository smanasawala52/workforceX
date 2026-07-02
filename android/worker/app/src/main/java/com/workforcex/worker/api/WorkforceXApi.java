package com.workforcex.worker.api;

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
            @Body WorkerProfileRequest request
    );

    @GET("api/worker/profile")
    Call<WorkerProfileResponse> getWorkerProfile(@Header("Authorization") String token);
}
