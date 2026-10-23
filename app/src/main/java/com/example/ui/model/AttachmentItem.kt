package com.example.ui.model

data class AttachmentItem(
    val type: String, // "image" or "text"
    val name: String,
    val data: String, // Base64 encoded or string content
    val previewUri: String? = null
)
