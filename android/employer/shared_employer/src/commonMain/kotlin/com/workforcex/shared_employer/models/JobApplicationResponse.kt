package com.workforcex.shared_employer.models

data class JobApplicationResponse(
    val applicationId: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val workerId: String,
    val workerName: String,
    val workerMobile: String,
    val workerSkills: String,
    val workerExperience: Int,
    val workerCity: String,
    val workerPreferredSalary: Double,
    val status: String, // PENDING, SHORTLISTED, REJECTED
    val appliedAt: String
)