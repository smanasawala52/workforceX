package com.workforcex.shared_employer.models

data class JobResponse(
    val id: String,
    val employerId: String,
    val title: String,
    val skillsRequired: String,
    val experienceRequired: Int,
    val location: String,
    val salaryMin: Double,
    val salaryMax: Double,
    val openPositions: Int,
    val description: String
)