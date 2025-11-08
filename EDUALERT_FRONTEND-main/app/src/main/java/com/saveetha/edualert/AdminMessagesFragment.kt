package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.ApiClient
import com.saveetha.edualert.GetAdminMessagesResponse
import com.saveetha.edualert.R
//import com.saveetha.edualert.AdminMessageAdapter
import com.saveetha.edualert.models.AdminMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminMessagesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adapter: AdminMessageAdapter
    private val messages = mutableListOf<AdminMessage>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_admin_messages, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewAdminMessages)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = AdminMessageAdapter(requireContext(), messages) {
            // Callback when message is updated - refresh the list and count
            fetchAdminMessages()
        }
        recyclerView.adapter = adapter

        fetchAdminMessages()
        return view
    }

    private fun fetchAdminMessages() {
        context?.let { ctx ->  // Safe context check
            // ✅ Get user ID from UserSession
            val userId = com.saveetha.edualert.UserSession.getUserId(ctx)

            if (userId.isNullOrEmpty()) {
                showEmpty("User ID not found.")
                return
            }

            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.GONE

            ApiClient.instance.getAdminMessages(userId).enqueue(object : Callback<GetAdminMessagesResponse> {
                override fun onResponse(
                    call: Call<GetAdminMessagesResponse>,
                    response: Response<GetAdminMessagesResponse>
                ) {
                    if (!isAdded) return  // fragment might be detached
                    progressBar.visibility = View.GONE
                    messages.clear()

                    val body = response.body()
                    if (body != null && body.status && !body.messages.isNullOrEmpty()) {
                        messages.addAll(body.messages)
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                    } else {
                        showEmpty("No messages available.")
                    }
                }

                override fun onFailure(call: Call<GetAdminMessagesResponse>, t: Throwable) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    Log.e("AdminMessagesFragment", "Failed to fetch admin messages", t)
                    showEmpty("Failed to fetch messages: ${t.message}")
                }
            })
        }
    }

    private fun showEmpty(message: String = "No messages available.") {
        if (!isAdded) return
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}
