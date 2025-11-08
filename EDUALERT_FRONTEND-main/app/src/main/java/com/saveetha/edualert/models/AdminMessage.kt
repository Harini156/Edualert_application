package com.saveetha.edualert.models

import com.google.gson.annotations.SerializedName

data class AdminMessage(
    val id: String,
    val title: String,
    val content: String,
    val attachment: String? = null,
    val recipient_type: String? = null,
    val department: String? = null,
    val staff_type: String? = null,
    val designation: String? = null,
    val year: String? = null,
    val stay_type: String? = null,
    val gender: String? = null,
    val cgpa: String? = null,
    val backlogs: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("message_table") val messageTable: String? = null,
    @SerializedName("user_status") val userStatus: String? = "unread"
)