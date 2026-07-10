package com.workforcex.shared_employer.models

data class LoginRequest(
    val mobileNumber: String,
    val password: String
)