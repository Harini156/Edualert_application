package com.saveetha.edualert

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val userType: String,
    val userId: String,
    val status: String, // "read" or "unread"
    val createdAt: String
)