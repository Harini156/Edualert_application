package com.saveetha.edualert

import android.content.Context
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

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
            // Build from global BASE_URL
            val url = ApiClient.BASE_URL + "api/get_message_count.php"
            
            // Debug logging
            android.util.Log.d("NOTIFICATION_MANAGER", "Loading message count for user: $userType, ID: $userId")
            android.util.Log.d("NOTIFICATION_MANAGER", "Request URL: $url")
            android.util.Log.d("NOTIFICATION_MANAGER", "Request Body: will send as form fields")
            
            val request = object : StringRequest(
                Method.POST,
                url,
                Response.Listener { responseString ->
                    try {
                        android.util.Log.d("NOTIFICATION_MANAGER", "Raw response: $responseString")
                        val response = JSONObject(responseString)
                        if (response.optString("status") == "success") {
                            val count = response.optInt("unread_count", 0)
                            val messagesCount = response.optInt("messages_count", 0)
                            val staffMessagesCount = response.optInt("staffmessages_count", 0)

                            android.util.Log.d(
                                "NOTIFICATION_MANAGER",
                                "Count: $count, Messages: $messagesCount, Staff: $staffMessagesCount"
                            )

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
                            android.util.Log.e(
                                "NOTIFICATION_MANAGER",
                                "API error status: ${response.optString("status")}"
                            )
                        }
                    } catch (e: Exception) {
                        badge.visibility = View.GONE
                        android.util.Log.e("NOTIFICATION_MANAGER", "JSON parse error: ${e.message}")
                    }
                },
                Response.ErrorListener { error ->
                    badge.visibility = View.GONE
                    android.util.Log.e("NOTIFICATION_MANAGER", "Network error: ${error.message}")
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["user_type"] = userType
                    params["user_id"] = userId
                    department?.let { params["department"] = it }
                    year?.let { params["year"] = it }
                    staffType?.let { params["staff_type"] = it }
                    designation?.let { params["designation"] = it }
                    return params
                }
            }
            
            Volley.newRequestQueue(context).add(request)
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
            // Show detailed debug info
            val debugInfo = "ID: $messageId (${messageId::class.simpleName}), Table: '$tableName' (${tableName::class.simpleName})"
            android.widget.Toast.makeText(context, debugInfo, android.widget.Toast.LENGTH_LONG).show()
            
            // Check if messageId is valid
            if (messageId <= 0) {
                android.widget.Toast.makeText(context, "ERROR: Invalid message ID: $messageId", android.widget.Toast.LENGTH_LONG).show()
                onError()
                return
            }
            
            // Check if tableName is valid
            if (tableName.isBlank()) {
                android.widget.Toast.makeText(context, "ERROR: Empty table name", android.widget.Toast.LENGTH_LONG).show()
                onError()
                return
            }
            
            // Direct API call to mark message as read
            val url = ApiClient.BASE_URL + "api/mark_message_read.php"
            val requestBody = JSONObject().apply {
                put("message_id", messageId.toString()) // Convert to string
                put("table_name", tableName)
            }
            
            android.widget.Toast.makeText(context, "Sending to API...", android.widget.Toast.LENGTH_SHORT).show()
            
            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                { response ->
                    val message = response.getString("message")
                    val status = response.getString("status")
                    android.widget.Toast.makeText(context, "Status: $status, Message: $message", android.widget.Toast.LENGTH_LONG).show()
                    
                    if (status == "success") {
                        onSuccess()
                    } else {
                        onError()
                    }
                },
                { error ->
                    android.widget.Toast.makeText(context, "Network Error: ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                    onError()
                }
            )
            
            Volley.newRequestQueue(context).add(request)
        }
    }
}

