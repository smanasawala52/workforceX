package com.workforcex.shared_employer.models

import com.google.gson.annotations.SerializedName

data class VerificationUpdateBody(
    @SerializedName("comments")
    val comments: String
)
