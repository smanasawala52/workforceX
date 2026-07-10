package com.workforcex.shared_employer.models

import com.google.gson.annotations.SerializedName

data class Verification(
    @SerializedName("id")
    val id: String,

    @SerializedName("user")
    val user: User,

    @SerializedName("verificationType")
    val verificationType: String,

    @SerializedName("status")
    val status: String
) {
    data class User(
        @SerializedName("mobileNumber")
        val mobileNumber: String
    )
}