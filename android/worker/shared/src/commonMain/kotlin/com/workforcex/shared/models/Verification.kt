package com.workforcex.shared.models

data class Verification(
    val id: String,
    val verificationType: String,
    val status: String,
    val reviewerComments: String
)