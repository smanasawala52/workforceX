package com.workforcex.shared.models

data class ResumeParseResult(
    val fileName: String,
    val extractedSkills: String,
    val detectedSkillList: List<String>,
    val detectedExperience: Int,
    val rawTextPreview: String,
    val message: String
)