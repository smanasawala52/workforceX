package com.workforcex.shared.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class JobBrowseItem(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("companyName")
    val companyName: String,
    @SerializedName("skillsRequired")
    val skillsRequired: String,
    @SerializedName("experienceRequired")
    val experienceRequired: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("salaryMin")
    val salaryMin: Double,
    @SerializedName("salaryMax")
    val salaryMax: Double,
    @SerializedName("openPositions")
    val openPositions: Int,
    @SerializedName("description")
    val description: String,
    var applied: Boolean = false // local UI state — not from API
) : Serializable