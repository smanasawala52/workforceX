package com.workforcex.shared_employer.models

data class CandidateSearchResult(
    val workerId: String, // Changed to String to match JSON
    val name: String,
    val mobileNumber: String,
    val skills: String,
    val experience: Int,
    val city: String,
    val state: String,
    val preferredSalary: Double,
    val totalScore: Double,
    val skillScore: Double,
    val experienceScore: Double,
    val locationScore: Double,
    val salaryScore: Double
)