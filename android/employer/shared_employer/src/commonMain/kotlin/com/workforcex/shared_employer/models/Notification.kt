package com.workforcex.shared_employer.models

import com.google.gson.annotations.SerializedName

data class Notification(
    @SerializedName("id")
    val id: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("read")
    val isRead: Boolean,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("linkType")
    val linkType: String,

    @SerializedName("linkId")
    val linkId: String,

    @SerializedName("jobTitle")
    val jobTitle: String
)