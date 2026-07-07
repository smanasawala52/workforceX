package com.workforcex.worker.api;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.RequestBody; // Added import
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

    @GET("api/jobs/browse")
    Call<List<JobBrowseItem>> browseJobs(@Header("Authorization") String token);

    @Multipart
    @POST("api/worker/resume")
    Call<ResumeParseResult> uploadResume(
            @Header("Authorization") String token,
            @Part MultipartBody.Part file);

    // Applications
    @POST("api/applications/{jobId}")
    Call<JobApplication> applyToJob(
            @Header("Authorization") String token,
            @Path("jobId") String jobId);

    @GET("api/applications/my")
    Call<List<JobApplication>> getMyApplications(@Header("Authorization") String token);

    @GET("api/notifications")
    Call<List<Notification>> getUnreadNotifications(@Header("Authorization") String token);

    // Verification
    @GET("api/verification/status")
    Call<List<Verification>> getVerificationStatus(@Header("Authorization") String token);

    @Multipart
    @POST("api/verification/upload")
    Call<Document> uploadDocument(
            @Header("Authorization") String token,
            @Part("type") RequestBody type,
            @Part MultipartBody.Part file);
}
