package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saveetha.edualert.adapters.MessageAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StaffSentFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private val sentList = mutableListOf<Message>()
    private lateinit var progressBar: ProgressBar
    private lateinit var senderId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_staff_sent, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewSentMessages)
        progressBar = view.findViewById(R.id.progressBar)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // ✅ Get logged-in staff ID from UserSession
        senderId = com.saveetha.edualert.UserSession.getUserId(requireContext()) ?: ""

        // ✅ Pass senderId to adapter
        adapter = MessageAdapter(requireContext(), sentList, senderId)
        recyclerView.adapter = adapter

        fetchStaffMessages()

        return view
    }

    private fun fetchStaffMessages() {
        progressBar.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE

        ApiClient.instance.getStaffMessages(senderId).enqueue(object : Callback<StaffMessagesResponse> {
            override fun onResponse(
                call: Call<StaffMessagesResponse>,
                response: Response<StaffMessagesResponse>
            ) {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE

                if (response.isSuccessful && response.body()?.status == "success") {
                    sentList.clear()
                    sentList.addAll(response.body()?.messages ?: emptyList())
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, "No messages found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<StaffMessagesResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
