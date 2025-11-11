package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminSentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SentMessagesAdapter
    private val messageList = mutableListOf<SentMessage>()
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_sent, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSentMessages)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = SentMessagesAdapter(requireContext(), messageList)
        recyclerView.adapter = adapter


        
        getSentMessages() // Fetch messages

        return view
    }

    private fun getSentMessages() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        ApiClient.instance.getMessages()
            .enqueue(object : Callback<GetMessagesResponse> {
                override fun onResponse(
                    call: Call<GetMessagesResponse>,
                    response: Response<GetMessagesResponse>
                ) {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE

                    if (response.isSuccessful && response.body()?.status == "success") {
                        val messages = response.body()?.messages ?: emptyList()
                        messageList.clear()
                        messageList.addAll(messages)
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to fetch messages",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<GetMessagesResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
    

}
