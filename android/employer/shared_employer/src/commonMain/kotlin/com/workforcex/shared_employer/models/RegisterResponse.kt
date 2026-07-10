package com.workforcex.shared_employer.models

data class RegisterResponse(
    val id: String,
    val countryCode: String,
    val mobileNumber: String,
    val fullMobileNumber: String,
    val role: String
)