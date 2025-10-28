package com.saveetha.edualert

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.saveetha.edualert.AdminMessagesFragment

class StaffHodFragment : Fragment() {
    private var notificationBadgeRef: TextView? = null
    private var debugInfo = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff_hod, container, false)

        val studentCard = view.findViewById<LinearLayout>(R.id.messageStudentsCard)
        val staffCard = view.findViewById<LinearLayout>(R.id.messageStaffCard)
        val messagesFromAdminCard = view.findViewById<LinearLayout>(R.id.messagesFromAdminCard)
        val viewSentMessagesCard = view.findViewById<LinearLayout>(R.id.viewSentMessagesCard)
        val notificationIconContainer = view.findViewById<View>(R.id.notificationIconContainer)
        val notificationBadge = view.findViewById<TextView>(R.id.notificationBadge)
        notificationBadgeRef = notificationBadge

        // ✅ Add Debug Button
        val debugButton = Button(requireContext()).apply {
            text = "🐛 DEBUG NOTIFICATIONS (HOD)"
            setOnClickListener { runDebugTests() }
        }
        
        // Add debug button to the layout
        if (view is ViewGroup) {
            view.addView(debugButton)
        }

        // Setup notification icon
        val userType = "staff" // HOD is also staff type
        val userId = getUserId()
        val staffType = getStaffType() // You'll need to implement this
        val designation = getDesignation() // You'll need to implement this
        
        NotificationManager.setupNotificationIcon(
            requireContext(),
            notificationIconContainer,
            notificationBadge,
            userType,
            userId,
            staffType = staffType,
            designation = designation
        )

        // 🔹 Student messages
        studentCard.setOnClickListener {
            val fragment = StaffSendMsg().apply {
                arguments = Bundle().apply { putString("recipientType", "student") }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // ✅ Activity container
                .addToBackStack(null)
                .commit()
        }

        // 🔹 Staff messages
        staffCard.setOnClickListener {
            val fragment = StaffSendMsg().apply {
                arguments = Bundle().apply { putString("recipientType", "staff") }
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment) // ✅ Activity container
                .addToBackStack(null)
                .commit()
        }

        // 🔹 Messages from Admin
        messagesFromAdminCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminMessagesFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🔹 View Sent Messages
        viewSentMessagesCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, StaffSentFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
    
    private fun getUserId(): String {
        return UserSession.getUserId(requireContext()) ?: "STF001"
    }
    
    private fun getStaffType(): String? {
        return UserSession.getStaffType(requireContext())
    }
    
    private fun getDesignation(): String? {
        return UserSession.getDesignation(requireContext())
    }
    
    private fun runDebugTests() {
        debugInfo.clear()
        debugInfo.append("=== HOD NOTIFICATION DEBUG ===\n\n")
        
        // 1. Check UserSession Data
        debugInfo.append("1. USER SESSION DATA:\n")
        debugInfo.append("User ID: ${getUserId()}\n")
        debugInfo.append("User Type: ${UserSession.getUserType(requireContext())}\n")
        debugInfo.append("Name: ${UserSession.getName(requireContext())}\n")
        debugInfo.append("Email: ${UserSession.getEmail(requireContext())}\n")
        debugInfo.append("Department: ${UserSession.getDepartment(requireContext())}\n")
        debugInfo.append("Staff Type: ${getStaffType()}\n")
        debugInfo.append("Designation: ${getDesignation()}\n")
        debugInfo.append("Is Logged In: ${UserSession.isLoggedIn(requireContext())}\n\n")
        
        // 2. Test API Call
        debugInfo.append("2. TESTING MESSAGE COUNT API:\n")
        debugInfo.append("API URL: ${ApiClient.BASE_URL}api/get_message_count.php\n")
        debugInfo.append("Request Parameters:\n")
        debugInfo.append("- user_type: staff\n")
        debugInfo.append("- user_id: ${getUserId()}\n")
        debugInfo.append("- department: ${UserSession.getDepartment(requireContext())}\n")
        debugInfo.append("- staff_type: ${getStaffType()}\n")
        debugInfo.append("- designation: ${getDesignation()}\n\n")
        
        // Make API call
        ApiClient.instance.getMessageCount(
            userType = "staff",
            userId = getUserId(),
            department = UserSession.getDepartment(requireContext()),
            staffType = getStaffType(),
            designation = getDesignation()
        ).enqueue(object : Callback<MessageCountResponse> {
            override fun onResponse(call: Call<MessageCountResponse>, response: Response<MessageCountResponse>) {
                debugInfo.append("3. API RESPONSE:\n")
                debugInfo.append("Response Code: ${response.code()}\n")
                debugInfo.append("Is Successful: ${response.isSuccessful}\n")
                
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    debugInfo.append("Status: ${body.status}\n")
                    debugInfo.append("Message: ${body.message}\n")
                    debugInfo.append("Unread Count: ${body.unread_count}\n")
                    debugInfo.append("Messages Count: ${body.messages_count}\n")
                    debugInfo.append("Staff Messages Count: ${body.staffmessages_count}\n")
                } else {
                    debugInfo.append("Response Body: NULL or Error\n")
                    debugInfo.append("Error Body: ${response.errorBody()?.string()}\n")
                }
                
                debugInfo.append("\n4. NOTIFICATION BADGE STATUS:\n")
                debugInfo.append("Badge Visibility: ${notificationBadgeRef?.visibility}\n")
                debugInfo.append("Badge Text: '${notificationBadgeRef?.text}'\n")
                
                showDebugResults()
            }
            
            override fun onFailure(call: Call<MessageCountResponse>, t: Throwable) {
                debugInfo.append("3. API FAILURE:\n")
                debugInfo.append("Error: ${t.message}\n")
                debugInfo.append("Cause: ${t.cause}\n")
                showDebugResults()
            }
        })
    }
    
    private fun showDebugResults() {
        val debugText = debugInfo.toString()
        
        // Copy to clipboard
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Debug Info", debugText)
        clipboard.setPrimaryClip(clip)
        
        // Show toast
        Toast.makeText(requireContext(), "Debug info copied to clipboard! Paste and send to developer.", Toast.LENGTH_LONG).show()
        
        // Also log to console
        android.util.Log.d("HOD_DEBUG", debugText)
    }
}
