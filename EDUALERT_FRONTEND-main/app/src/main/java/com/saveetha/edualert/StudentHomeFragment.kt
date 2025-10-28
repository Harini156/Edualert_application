package com.saveetha.edualert

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.saveetha.edualert.AdminMessagesFragment
import com.saveetha.edualert.ReceivedMessagesFragment

class StudentHomeFragment : Fragment() {
    private var notificationBadgeRef: TextView? = null
    private var debugInfo = StringBuilder()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_home, container, false)

        val messagesFromAdminCard = view.findViewById<LinearLayout>(R.id.messagesFromAdminCard)
        val messagesFromHodCard = view.findViewById<LinearLayout>(R.id.messagesFromHodCard)
        val notificationIconContainer = view.findViewById<View>(R.id.notificationIconContainer)
        val notificationIcon = notificationIconContainer.findViewById<ImageView>(R.id.notificationIcon)
        val notificationBadge = notificationIconContainer.findViewById<TextView>(R.id.notificationBadge)
        notificationBadgeRef = notificationBadge

        // ✅ Add Debug Button
        val debugButton = Button(requireContext()).apply {
            text = "🐛 DEBUG NOTIFICATIONS"
            setOnClickListener { runDebugTests() }
        }
        
        // Add debug button to the layout (assuming there's a main container)
        if (view is ViewGroup) {
            view.addView(debugButton)
        }

        // Setup notification icon
        val userType = "student"
        val userId = getUserId()
        val department = getDepartment() // You'll need to implement this
        val year = getYear() // You'll need to implement this
        
        NotificationManager.setupNotificationIcon(
            requireContext(),
            notificationIcon,
            notificationBadge,
            userType,
            userId,
            department,
            year
        )

        // Debug button removed as per request

        // 🔹 Messages from Admin
        messagesFromAdminCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AdminMessagesFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🔹 Messages from HOD
        messagesFromHodCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ReceivedMessagesFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }
    
    override fun onResume() {
        super.onResume()
        // Auto-refresh count whenever this screen is visible again
        val badge = notificationBadgeRef ?: return
        NotificationManager.refreshMessageCount(
            requireContext(),
            badge,
            "student",
            getUserId(),
            getDepartment(),
            getYear()
        )
    }
    
    private fun getUserId(): String {
        return UserSession.getUserId(requireContext()) ?: "STU001"
    }
    
    private fun getDepartment(): String? {
        return UserSession.getDepartment(requireContext())
    }
    
    private fun getYear(): String? {
        return UserSession.getYear(requireContext())
    }
    
    private fun runDebugTests() {
        debugInfo.clear()
        debugInfo.append("=== STUDENT NOTIFICATION DEBUG ===\n\n")
        
        // 1. Check UserSession Data
        debugInfo.append("1. USER SESSION DATA:\n")
        debugInfo.append("User ID: ${getUserId()}\n")
        debugInfo.append("User Type: ${UserSession.getUserType(requireContext())}\n")
        debugInfo.append("Name: ${UserSession.getName(requireContext())}\n")
        debugInfo.append("Email: ${UserSession.getEmail(requireContext())}\n")
        debugInfo.append("Department: ${getDepartment()}\n")
        debugInfo.append("Year: ${getYear()}\n")
        debugInfo.append("Is Logged In: ${UserSession.isLoggedIn(requireContext())}\n\n")
        
        // 2. Test API Call
        debugInfo.append("2. TESTING MESSAGE COUNT API:\n")
        debugInfo.append("API URL: ${ApiClient.BASE_URL}api/get_message_count.php\n")
        debugInfo.append("Request Parameters:\n")
        debugInfo.append("- user_type: student\n")
        debugInfo.append("- user_id: ${getUserId()}\n")
        debugInfo.append("- department: ${getDepartment()}\n")
        debugInfo.append("- year: ${getYear()}\n\n")
        
        // Make API call
        ApiClient.instance.getMessageCount(
            userType = "student",
            userId = getUserId(),
            department = getDepartment(),
            year = getYear()
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
        android.util.Log.d("STUDENT_DEBUG", debugText)
    }
}
