package com.workforcex.shared.models

data class WorkerProfileResponse(
    val id: String,
    val mobileNumber: String,
    val name: String,
    val gender: String,
    val city: String,
    val state: String,
    val skills: String,
    val experience: Int,
    val preferredSalary: Double,
    val availability: String,
    val languages: String
)