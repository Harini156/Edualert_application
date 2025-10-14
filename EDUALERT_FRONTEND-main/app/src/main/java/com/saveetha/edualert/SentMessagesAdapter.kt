package com.saveetha.edualert

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.ImageViewerActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SentMessagesAdapter(
    private val context: Context,
    private val messageList: MutableList<SentMessage>
) : RecyclerView.Adapter<SentMessagesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvDescription)
        val tvRecipientType: TextView = itemView.findViewById(R.id.tvRecipientType)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val tvStaffType: TextView = itemView.findViewById(R.id.tvStaffType)
        val tvDesignation: TextView = itemView.findViewById(R.id.tvDesignation)
        val tvYear: TextView = itemView.findViewById(R.id.tvYear)
        val tvStayType: TextView = itemView.findViewById(R.id.tvStayType)
        val tvGender: TextView = itemView.findViewById(R.id.tvGender)
        val tvCgpa: TextView = itemView.findViewById(R.id.tvCgpa)
        val tvBacklogs: TextView = itemView.findViewById(R.id.tvBacklogs)
        val tvAttachment: TextView = itemView.findViewById(R.id.tvAttachment)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_sent_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messageList[position]

        holder.tvTitle.text = msg.title
        holder.tvContent.text = msg.content

        // Dynamically show or hide optional fields
        fun updateField(textView: TextView, value: String?, prefix: String) {
            if (!value.isNullOrEmpty()) {
                textView.visibility = View.VISIBLE
                textView.text = "$prefix $value"
            } else {
                textView.visibility = View.GONE
            }
        }

        updateField(holder.tvRecipientType, msg.recipient_type, "Recipient:")
        updateField(holder.tvDepartment, msg.department, "Department:")
        updateField(holder.tvStaffType, msg.staff_type, "Staff Type:")
        updateField(holder.tvDesignation, msg.designation, "Designation:")
        updateField(holder.tvYear, msg.year, "Year:")
        updateField(holder.tvStayType, msg.stay_type, "Stay Type:")
        updateField(holder.tvGender, msg.gender, "Gender:")
        updateField(holder.tvCgpa, msg.cgpa, "CGPA:")
        updateField(holder.tvBacklogs, msg.backlogs, "Backlogs:")
        updateField(holder.tvCreatedAt, msg.created_at, "Created At:")

        // ✅ Show and handle attachment with blue underlined link
        if (!msg.attachment.isNullOrEmpty()) {
            holder.tvAttachment.visibility = View.VISIBLE
            holder.tvAttachment.text = "📎 View Attachment"
            holder.tvAttachment.setTextColor(
                ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            )
            holder.tvAttachment.paint.isUnderlineText = true

            holder.tvAttachment.setOnClickListener {
                try {
                    when {
                        // Image → ImageViewer
                        UrlUtils.resolveAttachmentUrl(msg.attachment).endsWith(".jpg", true) ||
                                msg.attachment.endsWith(".jpeg", true) ||
                                msg.attachment.endsWith(".png", true) -> {
                            val intent = Intent(context, ImageViewerActivity::class.java)
                            intent.putExtra("IMAGE_URL", UrlUtils.resolveAttachmentUrl(msg.attachment))
                            context.startActivity(intent)
                        }

                        // PDF → Installed PDF apps
                        msg.attachment.endsWith(".pdf", true) -> {
                            val uri = Uri.parse(UrlUtils.resolveAttachmentUrl(msg.attachment))
                            viewSelectedFile(context, uri)
                        }

                        // Word → Chrome → Docs
                        msg.attachment.endsWith(".doc", true) ||
                                msg.attachment.endsWith(".docx", true) -> {
                            openInChrome(context, UrlUtils.resolveAttachmentUrl(msg.attachment))
                        }

                        // Excel → Chrome → Sheets
                        msg.attachment.endsWith(".xls", true) ||
                                msg.attachment.endsWith(".xlsx", true) -> {
                            openInChrome(context, UrlUtils.resolveAttachmentUrl(msg.attachment))
                        }

                        else -> openFileInBrowser(UrlUtils.resolveAttachmentUrl(msg.attachment))
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open attachment", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        } else {
            holder.tvAttachment.visibility = View.GONE
        }

        // Delete with confirmation
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteMessage(msg.id, position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

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

    private fun openInChrome(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                setPackage("com.android.chrome")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(fallbackIntent)
        }
    }

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

    private fun deleteMessage(messageId: String, position: Int) {
        val idLong = messageId.toLongOrNull()
        if (idLong == null) {
            Toast.makeText(context, "Invalid message ID", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.deleteMessage(idLong, "admin")
            .enqueue(object : Callback<DeleteMessageResponse> {
                override fun onResponse(
                    call: Call<DeleteMessageResponse>,
                    response: Response<DeleteMessageResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == "success") {
                            messageList.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, messageList.size)
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Delete failed: ${body?.message ?: "Unknown error"}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Server Error: ${response.errorBody()?.string()}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<DeleteMessageResponse>, t: Throwable) {
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun getItemCount(): Int = messageList.size
}
