package com.saveetha.edualert.models

import com.google.gson.annotations.SerializedName

data class AdminMessage(
    val id: String,
    val title: String,
    val content: String,
    val attachment: String?,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("recipient_type")
    val recipientType: String?,  // "student", "staff", or "both"

    val department: String?,

    @SerializedName("staff_type")
    val staffType: String?,

    val designation: String?,
    val year: String?,

    @SerializedName("stay_type")
    val stayType: String?,

    val gender: String?,
    val cgpa: String?,
    val backlogs: String?
)
