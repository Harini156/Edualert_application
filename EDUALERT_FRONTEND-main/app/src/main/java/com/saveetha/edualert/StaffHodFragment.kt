package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.saveetha.edualert.AdminMessagesFragment

class StaffHodFragment : Fragment() {

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
}
