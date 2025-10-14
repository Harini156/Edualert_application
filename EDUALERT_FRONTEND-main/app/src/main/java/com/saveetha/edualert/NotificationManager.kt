package com.saveetha.edualert

import android.content.Context
import android.view.View
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.Response
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
            // Your computer's IP address from ipconfig
            val url = "http://192.168.1.7/get_message_count.php" // For real device
            
            val requestBody = JSONObject().apply {
                put("user_type", userType)
                put("user_id", userId)
                department?.let { put("department", it) }
                year?.let { put("year", it) }
                staffType?.let { put("staff_type", it) }
                designation?.let { put("designation", it) }
            }
            
            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                { response ->
                    try {
                        if (response.getString("status") == "success") {
                            val count = response.getInt("unread_count")
                            if (count > 0) {
                                badge.text = count.toString()
                                badge.visibility = View.VISIBLE
                            } else {
                                badge.visibility = View.GONE
                            }
                        } else {
                            badge.visibility = View.GONE
                        }
                    } catch (e: Exception) {
                        badge.visibility = View.GONE
                    }
                },
                { error ->
                    badge.visibility = View.GONE
                }
            )
            
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
            val url = "http://192.168.1.7/mark_message_read.php"
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

