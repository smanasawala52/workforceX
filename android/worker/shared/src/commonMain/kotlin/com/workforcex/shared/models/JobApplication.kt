package com.workforcex.shared.models

data class JobApplication(
    val applicationId: String,
    val jobId: String,
    val jobTitle: String,
    val companyName: String,
    val status: String, // PENDING, SHORTLISTED, REJECTED
    val appliedAt: String,
    val employerMobile: String
)