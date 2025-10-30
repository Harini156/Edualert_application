package com.saveetha.edualert

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
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
import com.google.android.material.button.MaterialButton
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

        // Add debug button for duplication analysis
        addDebugButton(view)
        
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
    
    private fun addDebugButton(rootView: View) {
        try {
            // Create small debug button
            val debugButton = MaterialButton(requireContext())
            debugButton.text = "🔧 DEBUG"
            debugButton.setBackgroundColor(Color.parseColor("#FF5722"))
            debugButton.setTextColor(Color.WHITE)
            debugButton.textSize = 10f
            debugButton.cornerRadius = 15
            
            // Set button click listener
            debugButton.setOnClickListener {
                val duplicateAnalysis = analyzeDuplicateMessages()
                
                // Copy to clipboard
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Duplicate Analysis", duplicateAnalysis)
                clipboard.setPrimaryClip(clip)
                
                Toast.makeText(requireContext(), "Duplicate analysis copied to clipboard!", Toast.LENGTH_LONG).show()
            }
            
            // Find the root LinearLayout and add button at the top
            val rootLayout = rootView as? LinearLayout
            if (rootLayout != null) {
                // Create layout params for small button
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.setMargins(16, 8, 16, 8)
                debugButton.layoutParams = layoutParams
                
                // Add button at the top (after the title row)
                rootLayout.addView(debugButton, 1)
                
                Toast.makeText(requireContext(), "Debug button added successfully!", Toast.LENGTH_SHORT).show()
            } else {
                showErrorWithCopy("Layout Error", "Root view is not LinearLayout: ${rootView.javaClass.simpleName}")
            }
            
        } catch (e: Exception) {
            showErrorWithCopy("Debug Button Error", "Error: ${e.message}\nStack: ${e.stackTraceToString()}")
        }
    }
    
    private fun showErrorWithCopy(title: String, errorMessage: String) {
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Copy Error") { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Error Info", "$title\n\n$errorMessage")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Error info copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            .create()
        
        alertDialog.show()
    }
    
    private fun analyzeDuplicateMessages(): String {
        val analysis = StringBuilder()
        analysis.append("=== DUPLICATE MESSAGE ANALYSIS ===\n")
        analysis.append("Total messages: ${messageList.size}\n")
        analysis.append("Timestamp: ${System.currentTimeMillis()}\n\n")
        
        // Group messages by title and content
        val messageGroups = messageList.groupBy { "${it.title}|${it.content}" }
        
        analysis.append("=== DUPLICATE DETECTION ===\n")
        var duplicateCount = 0
        
        messageGroups.forEach { (key, messages) ->
            if (messages.size > 1) {
                duplicateCount++
                analysis.append("DUPLICATE GROUP $duplicateCount:\n")
                analysis.append("Title: ${messages[0].title}\n")
                analysis.append("Content: ${messages[0].content}\n")
                analysis.append("Count: ${messages.size} copies\n")
                analysis.append("Timestamps:\n")
                messages.forEach { msg ->
                    analysis.append("  - ${msg.created_at}\n")
                }
                analysis.append("\n")
            }
        }
        
        if (duplicateCount == 0) {
            analysis.append("No duplicates found in current view.\n")
        } else {
            analysis.append("Total duplicate groups: $duplicateCount\n")
        }
        
        analysis.append("\n=== POSSIBLE CAUSES ===\n")
        analysis.append("1. Button not disabled during API call\n")
        analysis.append("2. Multiple click listeners attached\n")
        analysis.append("3. API called multiple times\n")
        analysis.append("4. Server-side duplication\n")
        analysis.append("5. Fragment recreated during send\n")
        
        return analysis.toString()
    }
}
