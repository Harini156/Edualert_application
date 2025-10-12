package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NotificationActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var adapter: NotificationAdapter
    private var notifications = mutableListOf<Notification>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)
        
        initViews()
        setupRecyclerView()
        loadNotifications()
        
        findViewById<ImageView>(R.id.backButton).setOnClickListener {
            finish()
        }
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.notificationsRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }
    
    private fun setupRecyclerView() {
        adapter = NotificationAdapter(notifications) { notification ->
            // Mark notification as read when clicked
            markNotificationAsRead(notification.id)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
    
    private fun loadNotifications() {
        val userType = intent.getStringExtra("user_type") ?: ""
        val userId = intent.getStringExtra("user_id") ?: ""
        
        if (userType.isEmpty() || userId.isEmpty()) {
            showEmptyState()
            return
        }
        
        val url = "http://your-server-url/api/get_notifications.php"
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
                    val notificationsArray = response.getJSONArray("notifications")
                    notifications.clear()
                    
                    for (i in 0 until notificationsArray.length()) {
                        val notificationJson = notificationsArray.getJSONObject(i)
                        val notification = Notification(
                            id = notificationJson.getInt("id"),
                            title = notificationJson.getString("title"),
                            message = notificationJson.getString("message"),
                            userType = notificationJson.getString("user_type"),
                            userId = notificationJson.getString("user_id"),
                            status = notificationJson.getString("status"),
                            createdAt = notificationJson.getString("created_at")
                        )
                        notifications.add(notification)
                    }
                    
                    adapter.notifyDataSetChanged()
                    
                    if (notifications.isEmpty()) {
                        showEmptyState()
                    } else {
                        showNotifications()
                    }
                } else {
                    showEmptyState()
                }
            },
            { error ->
                showEmptyState()
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun markNotificationAsRead(notificationId: Int) {
        val url = "http://your-server-url/api/mark_notification_read.php"
        val requestBody = JSONObject().apply {
            put("notification_id", notificationId)
        }
        
        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestBody,
            { response ->
                if (response.getString("status") == "success") {
                    // Update local notification status
                    val notification = notifications.find { it.id == notificationId }
                    notification?.let {
                        val index = notifications.indexOf(it)
                        notifications[index] = it.copy(status = "read")
                        adapter.notifyItemChanged(index)
                    }
                }
            },
            { error ->
                // Handle error silently
            }
        )
        
        Volley.newRequestQueue(this).add(request)
    }
    
    private fun showEmptyState() {
        recyclerView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
    }
    
    private fun showNotifications() {
        recyclerView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
    }
}

