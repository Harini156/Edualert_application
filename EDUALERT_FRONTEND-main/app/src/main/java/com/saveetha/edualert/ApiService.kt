package com.saveetha.edualert

import com.saveetha.edualert.models.Receivedmsg
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("api/register.php")
    fun registerUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("cpassword") cpassword: String,
        @Field("usertype") usertype: String
    ): Call<RegisterResponse>

    // LOGIN
    // ----------------------
    @FormUrlEncoded
    @POST("api/login.php")
    fun loginUser(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("role") role: String
    ): Call<LoginResponse>



    // Save Student Details
    @FormUrlEncoded
    @POST("api/student_details.php")
    fun saveStudentDetails(
        @Field("user_id") user_id: String,
        @Field("dob") dob: String,
        @Field("gender") gender: String,
        @Field("blood_group") blood_group: String,
        @Field("department") department: String,
        @Field("year") year: String,
        @Field("cgpa") cgpa: String,
        @Field("backlogs") backlogs: String,
        @Field("stay_type") stay_type: String,
        @Field("phone") phone: String,
        @Field("address") address: String
    ): Call<GenericResponse>

    // Save Staff Details
    @FormUrlEncoded
    @POST("api/staff_details.php")
    fun saveStaffDetails(
        @Field("user_id") user_id: String,
        @Field("dob") dob: String,
        @Field("staff_type") staff_type: String,
        @Field("department") department: String? = null,
        @Field("designation") designation: String? = null,
        @Field("phone") phone: String,
        @Field("address") address: String
    ): Call<GenericResponse>

    // ------------------------------
    // Reset Password APIs
    // ------------------------------

    @FormUrlEncoded
    @POST("api/send_otp.php")
    fun sendOtp(
        @Field("email") email: String
    ): Call<GenericResponse>

    @FormUrlEncoded
    @POST("api/reset_password.php")
    fun resetPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("new_password") newPassword: String
    ): Call<GenericResponse>


    // ------------------------------
    // ✅ New: Send Message API
    // ------------------------------
    @Multipart
    @POST("api/adminsendmsg.php")
    fun sendMessage(
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("recipient_type") recipientType: RequestBody,

        // optional filters
        @Part("department") department: RequestBody? = null,
        @Part("staff_type") staffType: RequestBody? = null,
        @Part("designation") designation: RequestBody? = null,
        @Part("year") year: RequestBody? = null,
        @Part("stay_type") stayType: RequestBody? = null,
        @Part("gender") gender: RequestBody? = null,
        @Part("cgpa") cgpa: RequestBody? = null,
        @Part("backlogs") backlogs: RequestBody? = null,

        // attachment (optional)
        @Part attachment: MultipartBody.Part? = null
    ): Call<MessageResponse>


    @FormUrlEncoded
    @POST("api/change_password.php")
    fun changePassword(
        @Field("email") email: String,
        @Field("old_password") oldPassword: String,
        @Field("new_password") newPassword: String
    ): Call<ChangePasswordResponse>


    // ✅ Fetch Sent Messages
    @GET("api/adminsent.php")
    fun getMessages(): Call<GetMessagesResponse>

    @FormUrlEncoded
    @POST("api/admindelete.php")
    fun deleteMessage(
        @Field("id") messageId: Long,
        @Field("usertype") userType: String
    ): Call<DeleteMessageResponse>

    @Multipart
    @POST("api/hodsendmessage.php")
    fun sendStaffMessage(
        @Part("sender_id") senderId: RequestBody,
        @Part("title") title: RequestBody,
        @Part("content") content: RequestBody,
        @Part("recipient_type") recipientType: RequestBody,
        @Part("department") department: RequestBody?,
        @Part("year") year: RequestBody?,
        @Part("designation") designation: RequestBody?,
        @Part attachment: MultipartBody.Part?
    ): Call<HodMessageResponse>

    @GET("api/staffsentmsg.php")
    fun getStaffSentMessages(
        @Query("sender_id") senderId: String
    ): Call<StaffMessagesResponse>


    @FormUrlEncoded
    @POST("api/gethodmsg.php")
    fun getReceivedMessages(
        @Field("user_id") userId: String
    ): Call<ReceivedMessagesResponse>

    // ✅ Get Student Messages (Admin + Staff messages for students)
    @FormUrlEncoded
    @POST("api/get_student_messages.php")
    fun getStudentMessages(
        @Field("user_id") userId: String
    ): Call<ReceivedMessagesResponse>

    // ✅ Get Staff Messages (Staff messages for staff members)
    @FormUrlEncoded
    @POST("api/get_staff_messages.php")
    fun getStaffMessages(
        @Field("user_id") userId: String
    ): Call<ReceivedMessagesResponse>

    @FormUrlEncoded
    @POST("api/staff_details.php")
    fun getStaffDetails(
        @Field("user_id") userId: String
    ): Call<StaffDetailsResponse>

    @FormUrlEncoded
    @POST("api/student_details.php")
    fun getStudentDetails(
        @Field("user_id") userId: String
    ): Call<StudentDetailsResponse>

    @FormUrlEncoded
    @POST("api/getadminmsg.php")
    fun getAdminMessages(
        @Field("user_id") userId: String
    ): Call<GetAdminMessagesResponse>



    @FormUrlEncoded
    @POST("api/staffdelete.php")
    fun staffDeleteMessage(
        @Field("message_id") messageId: Long,
        @Field("sender_id") senderId: String
    ): Call<StaffDeleteMessageResponse>

    // ✅ Message Count API
    @FormUrlEncoded
    @POST("api/get_message_count.php")
    fun getMessageCount(
        @Field("user_type") userType: String,
        @Field("user_id") userId: String,
        @Field("department") department: String? = null,
        @Field("year") year: String? = null,
        @Field("staff_type") staffType: String? = null,
        @Field("designation") designation: String? = null
    ): Call<MessageCountResponse>

    // ✅ Mark Message Status API (read/unread/deleted)
    @FormUrlEncoded
    @POST("api/mark_message_status.php")
    fun markMessageStatus(
        @Field("user_id") userId: String,
        @Field("message_id") messageId: String,
        @Field("message_table") messageTable: String,
        @Field("status") status: String
    ): Call<GenericResponse>

    // ✅ Get User Message Count API
    @FormUrlEncoded
    @POST("api/get_user_message_count.php")
    fun getUserMessageCount(
        @Field("user_id") userId: String,
        @Field("user_type") userType: String
    ): Call<MessageCountResponse>

    // ✅ Get Student Profile for Edit Mode
    @FormUrlEncoded
    @POST("api/studentprofile.php")
    fun getStudentProfile(
        @Field("student_id") userId: String
    ): Call<StudentDetailsResponse>

    // ✅ Get Staff Profile for Edit Mode
    @FormUrlEncoded
    @POST("api/staffprofile.php")
    fun getStaffProfile(
        @Field("staff_id") userId: String
    ): Call<StaffDetailsResponse>

}
