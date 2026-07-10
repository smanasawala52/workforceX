package com.workforcex.shared_employer.models

data class MatchedWorker(
    val workerId: String,
    val name: String,
    val mobileNumber: String,
    val skills: String,
    val experience: Int,
    val city: String,
    val preferredSalary: Double,
    val score: Double,
    val applicationStatus: String
)