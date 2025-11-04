package com.saveetha.edualert.models

import com.google.gson.annotations.SerializedName

data class Receivedmsg(
    val id: String,
    val title: String,
    val content: String,
    val attachment: String? = null,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("sender_type") val senderType: String,
    @SerializedName("sender_id") val senderId: String? = null,
    @SerializedName("sender_name") val senderName: String? = null,
    @SerializedName("message_table") val messageTable: String? = null,
    @SerializedName("user_status") val userStatus: String? = "unread"
)