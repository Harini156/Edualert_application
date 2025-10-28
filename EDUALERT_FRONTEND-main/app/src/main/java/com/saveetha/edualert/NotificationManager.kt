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
            
            // ✅ Use Retrofit instead of Volley
            ApiClient.instance.getMessageCount(
                userType = userType,
                userId = userId,
                department = department,
                year = year,
                staffType = staffType,
                designation = designation
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
            // Debug logging
            android.util.Log.d("NOTIFICATION_MANAGER", "Marking message as read: ID=$messageId, Table=$tableName")
            
            // Check if messageId is valid
            if (messageId <= 0) {
                android.util.Log.e("NOTIFICATION_MANAGER", "Invalid message ID: $messageId")
                onError()
                return
            }
            
            // Check if tableName is valid
            if (tableName.isBlank()) {
                android.util.Log.e("NOTIFICATION_MANAGER", "Empty table name")
                onError()
                return
            }
            
            // ✅ Use Retrofit instead of Volley
            ApiClient.instance.markMessageAsRead(
                messageId = messageId.toString(),
                tableName = tableName
            ).enqueue(object : retrofit2.Callback<GenericResponse> {
                override fun onResponse(
                    call: retrofit2.Call<GenericResponse>,
                    response: retrofit2.Response<GenericResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        android.util.Log.d("NOTIFICATION_MANAGER", "Mark read response: ${body.status} - ${body.message}")
                        
                        if (body.status == "success") {
                            onSuccess()
                        } else {
                            android.util.Log.e("NOTIFICATION_MANAGER", "Mark read failed: ${body.message}")
                            onError()
                        }
                    } else {
                        android.util.Log.e("NOTIFICATION_MANAGER", "Mark read response not successful: ${response.code()}")
                        onError()
                    }
                }

                override fun onFailure(call: retrofit2.Call<GenericResponse>, t: Throwable) {
                    android.util.Log.e("NOTIFICATION_MANAGER", "Mark read network error: ${t.message}")
                    onError()
                }
            })
        }
    }
}

