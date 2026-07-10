package com.workforcex.shared.models

data class LoginRequest(
    val mobileNumber: String,
    val password: String
)