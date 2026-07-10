package com.workforcex.shared.models

data class RegisterResponse(
    val id: String,
    val countryCode: String,
    val mobileNumber: String,
    val fullMobileNumber: String,
    val role: String
)