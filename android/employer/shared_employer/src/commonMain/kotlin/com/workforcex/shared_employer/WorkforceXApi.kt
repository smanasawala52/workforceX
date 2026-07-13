package com.workforcex.shared_employer

import com.workforcex.shared_employer.models.*
import retrofit2.Call
import retrofit2.http.*

interface WorkforceXApi {

    @POST("api/auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("api/auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @PUT("api/employer/profile")
    fun saveEmployerProfile(
        @Header("Authorization") token: String, @Body request: EmployerProfileRequest
    ): Call<EmployerProfileResponse>

    @GET("api/employer/profile")
    fun getEmployerProfile(@Header("Authorization") token: String): Call<EmployerProfileResponse>

    @POST("api/jobs")
    fun createJob(@Header("Authorization") token: String, @Body request: JobRequest): Call<JobResponse>

    @GET("api/jobs")
    fun getMyJobs(@Header("Authorization") token: String): Call<List<JobResponse>>

    @GET("api/jobs/{jobId}")
    fun getJobById(@Header("Authorization") token: String, @Path("jobId") jobId: String): Call<JobResponse>

    @PUT("api/jobs/{jobId}")
    fun updateJob(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String,
        @Body request: JobRequest
    ): Call<JobResponse>

    @DELETE("api/jobs/{jobId}")
    fun deleteJob(@Header("Authorization") token: String, @Path("jobId") jobId: String): Call<Void>

    @GET("api/matching/{jobId}")
    fun getMatchedWorkers(
        @Header("Authorization") token: String,
        @Path("jobId") jobId: String
    ): Call<List<MatchedWorker>>

    @POST("api/matching/search") // Changed to POST
    fun searchCandidates(
        @Header("Authorization") token: String,
        @Body request: CandidateSearchRequest
    ): Call<List<CandidateSearchResult>> // Changed to @Body

    // Applications
    @GET("api/applications/job/{jobId}")
    fun getApplicationsForJob(
        @Header("Authorization") token: String, @Path("jobId") jobId: String
    ): Call<List<JobApplicationItem>>

    @PUT("api/applications/{applicationId}/status")
    fun updateApplicationStatus(
        @Header("Authorization") token: String,
        @Path("applicationId") applicationId: String,
        @Query("status") status: String
    ): Call<JobApplicationItem>

    @POST("api/applications/offer")
    fun offerJob(
        @Header("Authorization") token: String,
        @Query("jobId") jobId: String,
        @Query("workerId") workerId: String
    ): Call<JobApplicationItem>

    // Notifications
    @GET("api/notifications")
    fun getUnreadNotifications(@Header("Authorization") token: String): Call<List<Notification>>

    @PUT("api/notifications/{notificationId}/read")
    fun markAsRead(@Header("Authorization") token: String, @Path("notificationId") notificationId: String): Call<Void>

    // Verification
    @GET("api/verification/pending")
    fun getPendingVerifications(@Header("Authorization") token: String): Call<List<Verification>>

    @GET("api/verification/worker/{workerId}")
    fun getWorkerDocuments(@Header("Authorization") token: String, @Path("workerId") workerId: String): Call<List<Verification>>

    // The actual uploaded files (filenames + view URLs) for a worker - what
    // an employer's profile view needs to show/open the real documents,
    // as opposed to getWorkerDocuments above, which despite its name only
    // returns coarse per-category verification status.
    @GET("api/verification/worker/{workerId}/documents")
    fun getWorkerDocumentFiles(
        @Header("Authorization") token: String,
        @Path("workerId") workerId: String
    ): Call<List<Document>>

    @PUT("api/verification/{verificationId}/status")
    fun updateEmployerVerificationStatus(
        @Header("Authorization") token: String,
        @Path("verificationId") verificationId: String,
        @Query("status") status: String,
        @Body comments: String
    ): Call<Void>

    // Skills
    @GET("api/skills")
    fun getSkills(): Call<List<Skill>>

    @GET("api/worker/profile/{workerId}")
    fun getWorkerProfile(@Header("Authorization") token: String, @Path("workerId") workerId: String): Call<WorkerProfileResponse>
}