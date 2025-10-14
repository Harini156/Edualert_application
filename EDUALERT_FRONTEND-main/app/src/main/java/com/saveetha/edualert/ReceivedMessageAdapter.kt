package com.saveetha.edualert.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.ImageViewerActivity
import com.saveetha.edualert.NotificationManager
import com.saveetha.edualert.R
import com.saveetha.edualert.models.Receivedmsg

class ReceivedMessageAdapter(
    private val context: Context,
    private val messages: List<Receivedmsg>
) : RecyclerView.Adapter<ReceivedMessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
        val tvAttachment: TextView = itemView.findViewById(R.id.tvAttachment)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val tickButton: ImageView = itemView.findViewById(R.id.tickButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]

        // Set title and content
        holder.tvTitle.text = msg.title
        holder.tvContent.text = msg.content

        // Handle tick button click
        holder.tickButton.setOnClickListener {
            // Determine which table this message belongs to
            // For now, we'll assume it's from messages table (admin messages)
            val tableName = "messages"
            
            // Debug: Show what we're working with
            val debugMsg = "Raw ID: '${msg.id}' (${msg.id::class.simpleName})"
            Toast.makeText(context, debugMsg, Toast.LENGTH_LONG).show()
            
            val messageId = try {
                // Try different parsing methods
                when {
                    msg.id is String -> msg.id.toInt()
                    msg.id is Int -> msg.id
                    msg.id is Long -> msg.id.toInt()
                    else -> msg.id.toString().toInt()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Parse Error: ${e.message}", Toast.LENGTH_LONG).show()
                0
            }
            
            if (messageId > 0) {
                NotificationManager.markMessageAsRead(
                    context,
                    messageId,
                    tableName,
                    onSuccess = {
                        // Hide tick button after marking as read
                        holder.tickButton.visibility = View.GONE
                        Toast.makeText(context, "Message marked as read", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(context, "Failed to mark as read", Toast.LENGTH_LONG).show()
                    }
                )
            } else {
                Toast.makeText(context, "Invalid message ID: ${msg.id}", Toast.LENGTH_LONG).show()
            }
        }

        // Handle attachment if available
        if (!msg.attachment.isNullOrEmpty()) {
            holder.tvAttachment.visibility = View.VISIBLE
            holder.tvAttachment.text = "📎 View Attachment"
            holder.tvAttachment.paint.isUnderlineText = true

            holder.tvAttachment.setOnClickListener {
                try {
                    when {
                        msg.attachment.endsWith(".jpg", true) ||
                                msg.attachment.endsWith(".jpeg", true) ||
                                msg.attachment.endsWith(".png", true) -> {
                            val intent = Intent(context, ImageViewerActivity::class.java)
                            intent.putExtra("IMAGE_URL", msg.attachment)
                            context.startActivity(intent)
                        }
                        msg.attachment.endsWith(".pdf", true) -> openFile(msg.attachment, "application/pdf")
                        msg.attachment.endsWith(".xls", true) -> openFile(msg.attachment, "application/vnd.ms-excel")
                        msg.attachment.endsWith(".xlsx", true) -> openFile(msg.attachment, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        msg.attachment.endsWith(".doc", true) -> openFile(msg.attachment, "application/msword")
                        msg.attachment.endsWith(".docx", true) -> openFile(msg.attachment, "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        else -> openFile(msg.attachment, "*/*")
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open attachment", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            holder.tvAttachment.visibility = View.GONE
        }

        // Format "Sent at" date
        holder.tvCreatedAt.text = try {
            val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val outputFormat = java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault())
            val date = inputFormat.parse(msg.created_at)
            if (date != null) "Sent at: ${outputFormat.format(date)}" else "Sent at: ${msg.created_at}"
        } catch (e: Exception) {
            "Sent at: ${msg.created_at}"
        }
    }

    private fun openFile(url: String, mimeType: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(Uri.parse(url), mimeType)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
