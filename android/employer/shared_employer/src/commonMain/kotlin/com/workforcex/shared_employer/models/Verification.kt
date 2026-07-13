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
    val status: String,

    @SerializedName("documents")
    val documents: List<Document> = emptyList()
) {
    data class User(
        @SerializedName("id")
        val id: String,
        @SerializedName("mobileNumber")
        val mobileNumber: String,
        @SerializedName("name")
        val name: String?
    )
}