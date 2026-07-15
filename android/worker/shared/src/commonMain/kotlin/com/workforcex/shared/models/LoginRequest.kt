package com.workforcex.shared.models

data class LoginRequest(
    val mobileNumber: String,
    val password: String,
    val countryCode: String = "+91"
)
