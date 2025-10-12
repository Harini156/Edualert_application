package com.saveetha.edualert

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val notifications: List<Notification>,
    private val onNotificationClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {
    
    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.notificationTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
        val timeTextView: TextView = itemView.findViewById(R.id.notificationTime)
        val unreadIndicator: View = itemView.findViewById(R.id.unreadIndicator)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        
        holder.titleTextView.text = notification.title
        holder.messageTextView.text = notification.message
        holder.timeTextView.text = formatTime(notification.createdAt)
        
        // Show unread indicator for unread notifications
        holder.unreadIndicator.visibility = if (notification.status == "unread") {
            View.VISIBLE
        } else {
            View.GONE
        }
        
        holder.itemView.setOnClickListener {
            onNotificationClick(notification)
        }
    }
    
    override fun getItemCount(): Int = notifications.size
    
    private fun formatTime(timeString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(timeString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            timeString
        }
    }
}

