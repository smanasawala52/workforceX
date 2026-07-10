package com.workforcex.shared_employer.models

data class CandidateSearchRequest(
    val skills: String,
    val city: String,
    val experienceMin: Int,
    val experienceMax: Int,
    val salaryMin: Double,
    val salaryMax: Double
)