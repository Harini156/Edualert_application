package com.saveetha.edualert.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.ApiClient
import com.saveetha.edualert.ImageViewerActivity
import com.saveetha.edualert.Message
import com.saveetha.edualert.R
import com.saveetha.edualert.StaffDeleteMessageResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MessageAdapter(
    private val context: Context,
    private val messages: MutableList<Message>,
    private val senderId: String // sender id passed from activity/fragment
) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvDescription)
        val tvRecipientType: TextView = itemView.findViewById(R.id.tvRecipientType)
        val tvDepartment: TextView = itemView.findViewById(R.id.tvDepartment)
        val tvYear: TextView = itemView.findViewById(R.id.tvYear)
        val tvDesignation: TextView = itemView.findViewById(R.id.tvDesignation)
        val tvAttachment: TextView = itemView.findViewById(R.id.tvAttachment)
        val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        val btnDelete: ImageView = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_staff_sent_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val msg = messages[position]

        holder.tvTitle.text = msg.title
        holder.tvContent.text = msg.content
        holder.tvCreatedAt.text = "Created At: ${msg.created_at}"

        fun setField(textView: TextView, value: String?, prefix: String) {
            if (!value.isNullOrEmpty() && value != "NULL") {
                textView.visibility = View.VISIBLE
                textView.text = "$prefix $value"
            } else {
                textView.visibility = View.GONE
            }
        }

        setField(holder.tvRecipientType, msg.recipient_type, "Recipient:")
        setField(holder.tvDepartment, msg.department, "Department:")
        setField(holder.tvYear, msg.year, "Year:")
        setField(holder.tvDesignation, msg.designation, "Designation:")

        // ✅ Attachment field as blue underlined link
        if (!msg.attachment.isNullOrEmpty() && msg.attachment != "NULL") {
            holder.tvAttachment.visibility = View.VISIBLE
            holder.tvAttachment.text = "📎 View Attachment"
            holder.tvAttachment.setTextColor(
                ContextCompat.getColor(context, android.R.color.holo_blue_dark)
            )
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

                        msg.attachment.endsWith(".pdf", true) -> {
                            val uri = Uri.parse(msg.attachment)
                            viewPdf(context, uri)
                        }

                        msg.attachment.endsWith(".doc", true) ||
                                msg.attachment.endsWith(".docx", true) ||
                                msg.attachment.endsWith(".xls", true) ||
                                msg.attachment.endsWith(".xlsx", true) -> {
                            openInBrowser(context, msg.attachment)
                        }

                        else -> openInBrowser(context, msg.attachment)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot open attachment", Toast.LENGTH_SHORT).show()
                    e.printStackTrace()
                }
            }
        } else {
            holder.tvAttachment.visibility = View.GONE
        }

        // Delete button
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Message")
                .setMessage("Are you sure you want to delete this message?")
                .setPositiveButton("Yes") { dialog, _ ->
                    deleteMessage(msg.id, senderId, position)
                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    private fun viewPdf(context: Context, uri: Uri) {
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

    private fun openInBrowser(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "No app found to open this file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMessage(messageId: String, senderId: String, position: Int) {
        val idLong = messageId.toLongOrNull()
        if (idLong == null) {
            Toast.makeText(context, "Invalid message ID", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.staffDeleteMessage(idLong, senderId)
            .enqueue(object : Callback<StaffDeleteMessageResponse> {
                override fun onResponse(
                    call: Call<StaffDeleteMessageResponse>,
                    response: Response<StaffDeleteMessageResponse>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body?.status == true) {
                            messages.removeAt(position)
                            notifyItemRemoved(position)
                            notifyItemRangeChanged(position, messages.size)
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show()

                            if (context is androidx.fragment.app.FragmentActivity) {
                                context.supportFragmentManager.popBackStack()
                            }
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

                override fun onFailure(call: Call<StaffDeleteMessageResponse>, t: Throwable) {
                    Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun getItemCount(): Int = messages.size
}
