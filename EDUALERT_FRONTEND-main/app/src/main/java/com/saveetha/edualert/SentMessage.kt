package com.saveetha.edualert

data class SentMessage(
    val id: String,
    val title: String,
    val content: String,
    val recipient_type: String,
    val department: String? = null,
    val staff_type: String? = null,
    val designation: String? = null,
    val year: String? = null,
    val stay_type: String? = null,
    val gender: String? = null,
    val cgpa: String? = null,
    val backlogs: String? = null,
    val attachment: String? = null,
    val created_at: String
)
