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
import com.saveetha.edualert.R
import com.saveetha.edualert.ReceivedMessagesResponse
import com.saveetha.edualert.adapters.ReceivedMessageAdapter
import com.saveetha.edualert.models.Receivedmsg
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReceivedMessagesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView
    private lateinit var adapter: ReceivedMessageAdapter
    private val messages = ArrayList<Receivedmsg>()

    private var userId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_received_messages, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMessages)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val sharedPref = requireContext().getSharedPreferences("EduAlertPrefs", Context.MODE_PRIVATE)
        userId = sharedPref.getString("USER_ID", null)

        // Adapter only takes context and messages now
        adapter = ReceivedMessageAdapter(requireContext(), messages)
        recyclerView.adapter = adapter

        if (userId == null) {
            showEmpty("User ID not found.")
        } else {
            fetchMessages()
        }

        return view
    }

    private fun fetchMessages() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.GONE

        Log.d("ReceivedMessages", "Fetching messages for user_id=$userId")

        ApiClient.instance.getReceivedMessages(userId!!).enqueue(object : Callback<ReceivedMessagesResponse> {
            override fun onResponse(
                call: Call<ReceivedMessagesResponse>,
                response: Response<ReceivedMessagesResponse>
            ) {
                progressBar.visibility = View.GONE
                messages.clear()

                val body = response.body()
                if (body != null) {
                    if (body.status && !body.messages.isNullOrEmpty()) {
                        messages.addAll(body.messages)
                        adapter.notifyDataSetChanged()
                        recyclerView.visibility = View.VISIBLE
                        emptyText.visibility = View.GONE
                    } else {
                        showEmpty(body.message ?: "No messages available.")
                    }
                } else {
                    showEmpty("Failed to load messages from server.")
                }
            }

            override fun onFailure(call: Call<ReceivedMessagesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                Log.e("ReceivedMessages", "Failed to fetch messages: ${t.message}", t)
                showEmpty("Failed to fetch messages: ${t.message}")
            }
        })
    }

    private fun showEmpty(message: String) {
        recyclerView.visibility = View.GONE
        emptyText.visibility = View.VISIBLE
        emptyText.text = message
    }
}
