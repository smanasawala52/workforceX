package com.workforcex.shared_employer.models

data class RegisterRequest(
    val mobileNumber: String,
    val role: String,
    val countryCode: String
)