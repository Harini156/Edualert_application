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
    val data: StaffData?   // nested object
)

data class StaffData(
    val user_id: String,
    val dob: String?,
    val staff_type: String,
    val department: String?,
    val designation: String?,
    val phone: String?,
    val address: String?
)


data class StudentDetailsResponse(
    val status: String,
    val message: String,
    val data: StudentData?  // Nested object containing student details
)

data class StudentData(
    val user_id: String,
    val department: String?,
    val year: String?,
    val gender: String?,
    val cgpa: String?,
    val stay_type: String?,
    val dob: String?,
    val blood_group: String?,
    val phone: String?,
    val address: String?,
    val backlogs: String?
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
    @SerializedName("created_at") val createdAt: String
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
