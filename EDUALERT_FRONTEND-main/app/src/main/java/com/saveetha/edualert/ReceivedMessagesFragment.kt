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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_received_messages, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewReceivedMessages)
        progressBar = view.findViewById(R.id.progressBar)
        emptyText = view.findViewById(R.id.emptyText)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReceivedMessageAdapter(requireContext(), messages)
        recyclerView.adapter = adapter

        fetchReceivedMessages()
        return view
    }

    private fun fetchReceivedMessages() {
        context?.let { ctx ->
            val userId = UserSession.getUserId(ctx)

            if (userId.isNullOrEmpty()) {
                showEmpty("User ID not found.")
                return
            }

            progressBar.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            emptyText.visibility = View.GONE

            ApiClient.instance.getReceivedMessages(userId).enqueue(object : Callback<ReceivedMessagesResponse> {
                override fun onResponse(
                    call: Call<ReceivedMessagesResponse>,
                    response: Response<ReceivedMessagesResponse>
                ) {
                    if (!isAdded) return
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

                override fun onFailure(call: Call<ReceivedMessagesResponse>, t: Throwable) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    Log.e("ReceivedMessagesFragment", "Failed to fetch received messages", t)
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