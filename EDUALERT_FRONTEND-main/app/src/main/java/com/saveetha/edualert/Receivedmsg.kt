package com.saveetha.edualert.models

data class Receivedmsg(
    val id: Int,
    val title: String,
    val content: String,
    val recipient_type: String,
    val department: String?,
    val year: String?,
    val designation: String?,
    val attachment: String?,
    val created_at: String
)

