package com.saveetha.edualert

data class Message(
    val id: String,
    val title: String,
    val content: String,
    val recipient_type: String,
    val department: String? = null,
    val year: String? = null,
    val designation: String? = null,
    val attachment: String? = null,
    val created_at: String
)
