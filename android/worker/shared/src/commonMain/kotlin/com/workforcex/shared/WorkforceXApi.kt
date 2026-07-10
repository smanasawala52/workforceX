package com.workforcex.shared

import com.workforcex.shared.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface WorkforceXApi {

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @PUT("api/worker/profile")
    fun saveWorkerProfile(
        @Header("Authorization") token: String,
        @Body request: WorkerProfileRequest
    ): Call<WorkerProfileResponse>

    @GET("api/worker/profile")
    fun getWorkerProfile(@Header("Authorization") token: String): Call<WorkerProfileResponse>

    @GET("api/jobs/browse")
    fun browseJobs(@Header("Authorization") token: String): Call<List<JobBrowseItem>>

    @Multipart
    @POST("api/worker/resume")
    fun uploadResume(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part
    ): Call<ResumeParseResult>

    // Applications
    @POST("api/applications/{jobId}")
    fun applyToJob(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Call<JobApplication>

    @GET("api/applications/my")
    fun getMyApplications(@Header("Authorization") token: String): Call<List<JobApplication>>

    @GET("api/notifications")
    fun getUnreadNotifications(@Header("Authorization") token: String): Call<List<Notification>>

    // Verification
    @GET("api/verification/status")
    fun getVerificationStatus(@Header("Authorization") token: String): Call<List<Verification>>

    @Multipart
    @POST("api/verification/upload")
    fun uploadDocument(
        @Header("Authorization") token: String,
        @Part("type") type: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<Document>

    // Skills
    @GET("api/skills")
    fun getSkills(): Call<List<Skill>>
}