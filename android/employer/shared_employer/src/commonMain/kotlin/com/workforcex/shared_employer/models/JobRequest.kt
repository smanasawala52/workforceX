package com.workforcex.shared_employer.models

data class JobRequest(
    val title: String,
    val skillsRequired: String,
    val experienceRequired: Int,
    val location: String,
    val salaryMin: Double,
    val salaryMax: Double,
    val openPositions: Int,
    val description: String
)