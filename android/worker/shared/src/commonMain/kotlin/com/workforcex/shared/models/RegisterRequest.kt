package com.workforcex.shared.models

data class RegisterRequest(
    val mobileNumber: String,
    val role: String,
    val countryCode: String
)