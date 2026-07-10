package com.workforcex.shared_employer.models

data class LoginResponse(
    val id: String,
    val countryCode: String,
    val mobileNumber: String,
    val fullMobileNumber: String,
    val role: String,
    val token: String
)