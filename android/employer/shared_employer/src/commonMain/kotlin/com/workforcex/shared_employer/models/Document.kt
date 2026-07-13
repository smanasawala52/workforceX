package com.workforcex.shared_employer.models

data class Document(
    val id: String,
    val documentType: String,
    val fileName: String,
    val fileUrl: String
)
