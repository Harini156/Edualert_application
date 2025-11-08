package com.saveetha.edualert

import com.google.gson.annotations.SerializedName
import com.saveetha.edualert.models.AdminMessage
import com.saveetha.edualert.models.Receivedmsg

data class RegisterResponse(
    val status: String,
    val message: String,
    val user_id: String?,
    val session: SessionData?,
    val department: String? = null,
    val year: String? = null
)

data class SessionData(
    val user_id: String,
    val name: String,
    val email: String,
    val usertype: String
)

data class GenericResponse(
    val status: String,
    val message: String
)

data class MessageResponse(
    val success: Boolean? = null,
    val message: String? = null,
    val error: String? = null
)

data class ChangePasswordResponse(
    val status: String,
    val message: String
)

data class GetMessagesResponse(
    val status: String,
    val messages: List<SentMessage>
)

data class DeleteMessageResponse(
    val status: String,
    val message: String
)

data class HodMessageResponse(
    val status: Boolean,
    val message: String
)
data class StaffMessagesResponse(
    val status: String,
    val messages: List<Message>
)

data class StaffDetailsResponse(
    val status: String,
    val message: String,
    val data: StaffData? = null,   // For login API
    val staff: StaffData? = null   // For profile API
)

data class StaffData(
    val user_id: String,
    val name: String? = null,
    val email: String? = null,
    val dob: String? = null,
    val staff_type: String? = null,
    val department: String? = null,
    val designation: String? = null,
    val phone: String? = null,
    val address: String? = null
)


data class StudentDetailsResponse(
    val status: String,
    val message: String,
    val data: StudentData? = null,  // For login API
    val student: StudentData? = null  // For profile API
)

data class StudentData(
    val user_id: String,
    val name: String? = null,
    val email: String? = null,
    val department: String? = null,
    val year: String? = null,
    val gender: String? = null,
    val cgpa: String? = null,
    val stay_type: String? = null,
    val dob: String? = null,
    val blood_group: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val backlogs: String? = null
)


data class GetAdminMessagesResponse(
    val status: Boolean,
    val messages: List<AdminMessage>
)

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


data class LoginResponse(
    val status: String,
    val message: String,
    val user: User
)

data class User(
    val user_id: String,
    val name: String,
    val email: String,
    val user_type: String
)

data class StaffDeleteMessageResponse(
    val status: Boolean,
    val message: String
)
data class ReceivedMessagesResponse(
    val status: Boolean,
    val messages: List<Receivedmsg>?,
    val message: String? = null  // Add this line
)

// ✅ Message Count Response
data class MessageCountResponse(
    val status: String,
    val message: String? = null,
    val unread_count: Int = 0,
    val messages_count: Int = 0,
    val staffmessages_count: Int = 0
)