package com.saveetha.edualert

import android.content.Context
import android.content.Intent
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
            userId: String
        ) {
            // Set click listener to open notification activity
            notificationIcon.setOnClickListener {
                val intent = Intent(context, NotificationActivity::class.java).apply {
                    putExtra("user_type", userType)
                    putExtra("user_id", userId)
                }
                context.startActivity(intent)
            }
            
            // Load notification count
            loadNotificationCount(context, notificationBadge, userType, userId)
        }
        
        private fun loadNotificationCount(
            context: Context,
            badge: TextView,
            userType: String,
            userId: String
        ) {
            val url = "http://your-server-url/api/get_notification_count.php"
            val requestBody = JSONObject().apply {
                put("user_type", userType)
                put("user_id", userId)
            }
            
            val request = JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                { response ->
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
                },
                { error ->
                    badge.visibility = View.GONE
                }
            )
            
            Volley.newRequestQueue(context).add(request)
        }
        
        fun refreshNotificationCount(
            context: Context,
            badge: TextView,
            userType: String,
            userId: String
        ) {
            loadNotificationCount(context, badge, userType, userId)
        }
    }
}

