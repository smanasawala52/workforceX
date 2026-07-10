package com.workforcex.shared_employer.models

data class EmployerProfileResponse(
    val id: String,
    val mobileNumber: String,
    val companyName: String,
    val contactPerson: String,
    val email: String,
    val address: String
)