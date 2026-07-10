package com.workforcex.shared_employer.models

data class EmployerProfileRequest(
    val companyName: String,
    val contactPerson: String,
    val email: String,
    val address: String
)