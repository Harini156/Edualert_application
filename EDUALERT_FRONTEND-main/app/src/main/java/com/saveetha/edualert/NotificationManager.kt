package com.saveetha.edualert

import android.content.Context
import android.view.View
import android.widget.TextView

class NotificationManager {
    
    companion object {
        fun setupNotificationIcon(
            context: Context,
            notificationIcon: View,
            notificationBadge: TextView,
            userType: String,
            userId: String,
            department: String? = null,
            year: String? = null,
            staffType: String? = null,
            designation: String? = null
        ) {
            // Debug logging
            android.util.Log.d("NOTIFICATION_MANAGER", "setupNotificationIcon called")
            android.util.Log.d("NOTIFICATION_MANAGER", "Badge initial visibility: ${notificationBadge.visibility}")
            android.util.Log.d("NOTIFICATION_MANAGER", "Badge initial text: '${notificationBadge.text}'")
            
            // No click listener - bell icon only shows count
            // Load notification count
            loadMessageCount(context, notificationBadge, userType, userId, department, year, staffType, designation)
        }
        
        private fun loadMessageCount(
            context: Context,
            badge: TextView,
            userType: String,
            userId: String,
            department: String? = null,
            year: String? = null,
            staffType: String? = null,
            designation: String? = null
        ) {
            // Debug logging
            android.util.Log.d("NOTIFICATION_MANAGER", "Loading message count for user: $userType, ID: $userId")
            android.util.Log.d("NOTIFICATION_MANAGER", "Department: $department, Year: $year")
            android.util.Log.d("NOTIFICATION_MANAGER", "StaffType: $staffType, Designation: $designation")
            
            // ✅ Use new user-specific message count API
            ApiClient.instance.getUserMessageCount(
                userId = userId,
                userType = userType
            ).enqueue(object : retrofit2.Callback<MessageCountResponse> {
                override fun onResponse(
                    call: retrofit2.Call<MessageCountResponse>,
                    response: retrofit2.Response<MessageCountResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        android.util.Log.d("NOTIFICATION_MANAGER", "API Response: ${body}")
                        
                        if (body.status == "success") {
                            val count = body.unread_count
                            android.util.Log.d("NOTIFICATION_MANAGER", "Unread count: $count")

                            if (count > 0) {
                                badge.text = count.toString()
                                badge.visibility = View.VISIBLE
                                android.util.Log.d("NOTIFICATION_MANAGER", "Badge visible with count: $count")
                            } else {
                                badge.text = ""
                                badge.visibility = View.GONE
                                android.util.Log.d("NOTIFICATION_MANAGER", "No unread, badge hidden")
                            }
                        } else {
                            badge.visibility = View.GONE
                            android.util.Log.e("NOTIFICATION_MANAGER", "API error: ${body.message}")
                        }
                    } else {
                        badge.visibility = View.GONE
                        android.util.Log.e("NOTIFICATION_MANAGER", "Response not successful: ${response.code()}")
                    }
                }

                override fun onFailure(call: retrofit2.Call<MessageCountResponse>, t: Throwable) {
                    badge.visibility = View.GONE
                    android.util.Log.e("NOTIFICATION_MANAGER", "Network error: ${t.message}")
                }
            })
        }
        
        fun refreshMessageCount(
            context: Context,
            badge: TextView,
            userType: String,
            userId: String,
            department: String? = null,
            year: String? = null,
            staffType: String? = null,
            designation: String? = null
        ) {
            loadMessageCount(context, badge, userType, userId, department, year, staffType, designation)
        }
        
        fun markMessageAsRead(
            context: Context,
            messageId: Int,
            tableName: String,
            onSuccess: () -> Unit = {},
            onError: () -> Unit = {}
        ) {
            // ✅ Enhanced debug info for tick button issues
            val debugInfo = StringBuilder()
            debugInfo.append("=== MARK MESSAGE AS READ DEBUG ===\n")
            debugInfo.append("Message ID: $messageId (Type: ${messageId::class.simpleName})\n")
            debugInfo.append("Table Name: '$tableName' (Type: ${tableName::class.simpleName})\n")
            debugInfo.append("API URL: ${ApiClient.BASE_URL}api/mark_message_read.php\n")
            
            android.util.Log.d("NOTIFICATION_MANAGER", debugInfo.toString())
            
            // Check if messageId is valid
            if (messageId <= 0) {
                debugInfo.append("ERROR: Invalid message ID: $messageId\n")
                android.widget.Toast.makeText(context, debugInfo.toString(), android.widget.Toast.LENGTH_LONG).show()
                android.util.Log.e("NOTIFICATION_MANAGER", "Invalid message ID: $messageId")
                onError()
                return
            }
            
            // Check if tableName is valid
            if (tableName.isBlank()) {
                debugInfo.append("ERROR: Empty table name\n")
                android.widget.Toast.makeText(context, debugInfo.toString(), android.widget.Toast.LENGTH_LONG).show()
                android.util.Log.e("NOTIFICATION_MANAGER", "Empty table name")
                onError()
                return
            }
            
            debugInfo.append("Sending API request...\n")
            
            // ✅ Use Retrofit instead of Volley
            ApiClient.instance.markMessageAsRead(
                messageId = messageId.toString(),
                tableName = tableName
            ).enqueue(object : retrofit2.Callback<GenericResponse> {
                override fun onResponse(
                    call: retrofit2.Call<GenericResponse>,
                    response: retrofit2.Response<GenericResponse>
                ) {
                    debugInfo.append("API Response Code: ${response.code()}\n")
                    debugInfo.append("Is Successful: ${response.isSuccessful}\n")
                    
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        debugInfo.append("Response Status: ${body.status}\n")
                        debugInfo.append("Response Message: ${body.message}\n")
                        
                        android.util.Log.d("NOTIFICATION_MANAGER", "Mark read response: ${body.status} - ${body.message}")
                        
                        if (body.status == "success") {
                            debugInfo.append("SUCCESS: Message marked as read\n")
                            android.widget.Toast.makeText(context, "✅ Message marked as read successfully!", android.widget.Toast.LENGTH_SHORT).show()
                            onSuccess()
                        } else {
                            debugInfo.append("FAILED: ${body.message}\n")
                            android.widget.Toast.makeText(context, debugInfo.toString(), android.widget.Toast.LENGTH_LONG).show()
                            android.util.Log.e("NOTIFICATION_MANAGER", "Mark read failed: ${body.message}")
                            onError()
                        }
                    } else {
                        debugInfo.append("ERROR: Response not successful or body is null\n")
                        debugInfo.append("Error Body: ${response.errorBody()?.string()}\n")
                        android.widget.Toast.makeText(context, debugInfo.toString(), android.widget.Toast.LENGTH_LONG).show()
                        android.util.Log.e("NOTIFICATION_MANAGER", "Mark read response not successful: ${response.code()}")
                        onError()
                    }
                }

                override fun onFailure(call: retrofit2.Call<GenericResponse>, t: Throwable) {
                    debugInfo.append("NETWORK ERROR: ${t.message}\n")
                    debugInfo.append("Cause: ${t.cause}\n")
                    android.widget.Toast.makeText(context, debugInfo.toString(), android.widget.Toast.LENGTH_LONG).show()
                    android.util.Log.e("NOTIFICATION_MANAGER", "Mark read network error: ${t.message}")
                    onError()
                }
            })
        }
    }
}

