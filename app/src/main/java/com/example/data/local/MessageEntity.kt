package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val role: String, // "user" or "assistant" or "error"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val attachmentType: String? = null, // "image" or "file"
    val attachmentData: String? = null, // Base64 data or text preview
    val attachmentName: String? = null,
    val isFeedbackPositive: Boolean? = null // null, true, false
)
