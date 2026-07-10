package com.workforcex.shared.models

data class Document(
    val id: String,
    val documentType: String,
    val fileName: String,
    val fileUrl: String
)