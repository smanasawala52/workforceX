package com.workforcex.shared_employer.models

import com.google.gson.annotations.SerializedName

data class Skill(
    @SerializedName("name")
    val name: String
)