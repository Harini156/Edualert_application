package com.saveetha.edualert

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
import com.saveetha.edualert.models.AdminMessage
import java.text.SimpleDateFormat
import java.util.Locale

class AdminMessageAdapter(
    private val context: Context,
    private val messages: List<AdminMessage>
) : RecyclerView.Adapter<AdminMessageAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvDescription)
        val tvAttachment: TextView = itemView.findViewById(R.id.tvAttachment)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val tickButton: ImageView = itemView.findViewById(R.id.tickButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvTitle.text = msg.title
        holder.tvContent.text = msg.content

        // Handle tick button click
        holder.tickButton.setOnClickListener {
            // Admin messages are stored in messages table
            val tableName = "messages"
            val messageId = try {
                msg.id.toInt()
            } catch (e: NumberFormatException) {
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
                        Toast.makeText(context, "Failed to mark as read", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        if (!msg.attachment.isNullOrEmpty()) {
            holder.tvAttachment.visibility = View.VISIBLE
            holder.tvAttachment.text = "📎 View Attachment"
            holder.tvAttachment.paint.isUnderlineText = true

            holder.tvAttachment.setOnClickListener {
                try {
                    when {
                        // ✅ Images
                        msg.attachment.endsWith(".jpg", true) ||
                                msg.attachment.endsWith(".jpeg", true) ||
                                msg.attachment.endsWith(".png", true) -> {
                            val intent = Intent(context, ImageViewerActivity::class.java)
                            intent.putExtra("IMAGE_URL", msg.attachment)
                            context.startActivity(intent)
                        }

                        // ✅ PDFs
                        msg.attachment.endsWith(".pdf", true) -> {
                            val uri = Uri.parse(msg.attachment)
                            viewSelectedFile(context, uri)
                        }

                        // ✅ Word files (open in Chrome → Google Docs)
                        msg.attachment.endsWith(".doc", true) ||
                                msg.attachment.endsWith(".docx", true) -> {
                            openInChrome(context, msg.attachment)
                        }

                        // ✅ Excel files (open in Chrome → Google Sheets)
                        msg.attachment.endsWith(".xls", true) ||
                                msg.attachment.endsWith(".xlsx", true) -> {
                            openInChrome(context, msg.attachment)
                        }

                        else -> openFileInBrowser(msg.attachment)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open attachment", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        } else {
            holder.tvAttachment.visibility = View.GONE
        }

        // Sent at
        holder.tvCreatedAt.text = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(msg.createdAt)
            if (date != null) "Sent at: ${outputFormat.format(date)}" else "Sent at: ${msg.createdAt}"
        } catch (e: Exception) {
            "Sent at: ${msg.createdAt}"
        }
    }

    // 🔹 Open PDF with installed PDF apps
    private fun viewSelectedFile(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    // 🔹 Force open in Chrome (Docs/Sheets handle Word/Excel)
    private fun openInChrome(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.android.chrome") // Force Chrome
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // fallback → any browser
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }
    }

    // 🔹 Generic browser open (fallback)
    private fun openFileInBrowser(fileUrl: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }
}
