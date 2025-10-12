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
            // TODO: Replace with your computer's IP address
            // Find your IP by running 'ipconfig' in Command Prompt
            val url = "http://YOUR_COMPUTER_IP/edualert/api/get_message_count.php" // For real device
            // Example: "http://192.168.1.100/edualert/api/get_message_count.php"
            
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
            val url = "http://YOUR_COMPUTER_IP/edualert/api/mark_message_read.php" // For real device
            val requestBody = JSONObject().apply {
                put("message_id", messageId)
                put("table_name", tableName)
            }
            
            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                { response ->
                    if (response.getString("status") == "success") {
                        onSuccess()
                    } else {
                        onError()
                    }
                },
                { error ->
                    onError()
                }
            )
            
            Volley.newRequestQueue(context).add(request)
        }
    }
}

