package com.saveetha.edualert

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.models.AdminMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class AdminMessageAdapter(
    private val context: Context,
    private val messages: MutableList<AdminMessage>,
    private val onMessageUpdated: (() -> Unit)? = null
) : RecyclerView.Adapter<AdminMessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val contentText: TextView = itemView.findViewById(R.id.contentText)
        val senderText: TextView = itemView.findViewById(R.id.senderText)
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val attachmentText: TextView = itemView.findViewById(R.id.attachmentText)
        val tickButton: ImageView = itemView.findViewById(R.id.tickButton)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_received_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        
        holder.titleText.text = message.title
        holder.contentText.text = message.content
        holder.senderText.text = "From: Admin"
        
        // Format date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(message.createdAt)
            holder.dateText.text = date?.let { outputFormat.format(it) } ?: message.createdAt
        } catch (e: Exception) {
            holder.dateText.text = message.createdAt
        }
        
        // Handle attachment with clickable link
        if (!message.attachment.isNullOrEmpty()) {
            holder.attachmentText.visibility = View.VISIBLE
            val fileName = message.attachment.substringAfterLast("/")
            holder.attachmentText.text = "📎 View Attachment: $fileName"
            holder.attachmentText.setTextColor(context.getColor(android.R.color.holo_blue_dark))
            holder.attachmentText.setOnClickListener {
                openAttachment(message.attachment)
            }
        } else {
            holder.attachmentText.visibility = View.GONE
        }

        // Use backend status just like staff messages
        val isRead = message.userStatus == "read"
        
        holder.tickButton.setImageResource(
            if (isRead) android.R.drawable.checkbox_on_background 
            else android.R.drawable.checkbox_off_background
        )

        // Show delete button only if message is read
        holder.deleteButton.visibility = if (isRead) View.VISIBLE else View.GONE

        // Tick button click listener
        holder.tickButton.setOnClickListener {
            if (!isRead) {
                markMessageAsRead(message, position)
            }
        }

        // Delete button click listener
        holder.deleteButton.setOnClickListener {
            deleteMessage(message, position)
        }
    }

    private fun markMessageAsRead(message: AdminMessage, position: Int) {
        val userId = UserSession.getUserId(context) ?: return

        ApiClient.instance.markMessageStatus(
            userId, message.id, "messages", "read"
        ).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Update local message status to reflect the change
                    messages[position] = message.copy(userStatus = "read")
                    notifyItemChanged(position)
                    onMessageUpdated?.invoke() // Refresh count
                    Toast.makeText(context, "Message marked as read", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteMessage(message: AdminMessage, position: Int) {
        val userId = UserSession.getUserId(context) ?: return

        ApiClient.instance.markMessageStatus(
            userId, message.id, "messages", "deleted"
        ).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    // Remove message from list
                    messages.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, messages.size)
                    onMessageUpdated?.invoke() // Refresh count
                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to delete message", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun openAttachment(attachmentPath: String) {
        try {
            val baseUrl = ApiClient.BASE_URL
            val fileUrl = "${baseUrl}api/get_file.php?file=${attachmentPath}"
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(fileUrl)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
            
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open attachment: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun getItemCount(): Int = messages.size
}