package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.saveetha.edualert.AdminMessagesFragment
import com.saveetha.edualert.ReceivedMessagesFragment

class StudentHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_home, container, false)

        val messagesFromAdminCard = view.findViewById<LinearLayout>(R.id.messagesFromAdminCard)
        val messagesFromHodCard = view.findViewById<LinearLayout>(R.id.messagesFromHodCard)
        val notificationIconContainer = view.findViewById<View>(R.id.notificationIconContainer)
        val notificationBadge = view.findViewById<TextView>(R.id.notificationBadge)
        val debugButton = view.findViewById<Button>(R.id.debugButton)

        // Setup notification icon
        val userType = "student"
        val userId = getUserId()
        val department = getDepartment() // You'll need to implement this
        val year = getYear() // You'll need to implement this
        
        NotificationManager.setupNotificationIcon(
            requireContext(),
            notificationIconContainer,
            notificationBadge,
            userType,
            userId,
            department,
            year
        )

        // Setup debug button
        debugButton.setOnClickListener {
            val intent = android.content.Intent(requireContext(), DebugActivity::class.java)
            startActivity(intent)
        }

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
    
    private fun getUserId(): String {
        // TODO: Implement method to get current user ID from SharedPreferences or session
        // For now, return a placeholder
        return "STU001"
    }
    
    private fun getDepartment(): String? {
        // TODO: Implement method to get current user department from SharedPreferences or session
        // For now, return a placeholder
        return "CSE"
    }
    
    private fun getYear(): String? {
        // TODO: Implement method to get current user year from SharedPreferences or session
        // For now, return a placeholder
        return "2"
    }
}
