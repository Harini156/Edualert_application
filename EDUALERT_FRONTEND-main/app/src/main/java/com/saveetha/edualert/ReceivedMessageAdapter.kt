package com.saveetha.edualert

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.models.Receivedmsg
import java.text.SimpleDateFormat
import java.util.*

class ReceivedMessageAdapter(
    private val context: Context,
    private val messages: List<Receivedmsg>
) : RecyclerView.Adapter<ReceivedMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val contentText: TextView = itemView.findViewById(R.id.contentText)
        val senderText: TextView = itemView.findViewById(R.id.senderText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val attachmentText: TextView = itemView.findViewById(R.id.attachmentText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        
        holder.titleText.text = message.title
        holder.contentText.text = message.content
        
        // Set sender info
        val senderName = message.senderName ?: "Unknown"
        val senderType = when(message.senderType) {
            "admin" -> "Admin"
            "staff" -> "Staff"
            else -> "System"
        }
        holder.senderText.text = "From: $senderName ($senderType)"
        
        // Format date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(message.createdAt)
            holder.dateText.text = date?.let { outputFormat.format(it) } ?: message.createdAt
        } catch (e: Exception) {
            holder.dateText.text = message.createdAt
        }
        
        // Handle attachment
        if (!message.attachment.isNullOrEmpty()) {
            holder.attachmentText.visibility = View.VISIBLE
            holder.attachmentText.text = "📎 Attachment: ${message.attachment}"
        } else {
            holder.attachmentText.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = messages.size
}