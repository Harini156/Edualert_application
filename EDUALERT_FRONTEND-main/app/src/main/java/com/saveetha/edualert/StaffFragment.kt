package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.saveetha.edualert.AdminMessagesFragment
import com.saveetha.edualert.ReceivedMessagesFragment

class StaffFragment : Fragment() {

    private var staffType: String? = null  // Teaching / Non-Teaching
    private var notificationBadgeRef: TextView? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff, container, false)

        val messagesFromAdminCard = view.findViewById<LinearLayout>(R.id.messagesFromAdminCard)
        val messagesFromHodCard = view.findViewById<LinearLayout>(R.id.messagesFromHodCard)
        val notificationIconContainer = view.findViewById<View>(R.id.notificationIconContainer)
        val notificationBadge = view.findViewById<TextView>(R.id.notificationBadge)
        notificationBadgeRef = notificationBadge

        // Get staff type from arguments
        staffType = arguments?.getString("staff_type", "Teaching")



        // Setup notification icon
        val userType = "staff"
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

        // Normalize staff type for consistent comparison
        val normalizedStaffType = staffType?.trim()?.lowercase()
        if (normalizedStaffType == "non-teaching") {
            messagesFromHodCard.visibility = View.GONE
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

    override fun onResume() {
        super.onResume()
        val badge = notificationBadgeRef ?: return
        NotificationManager.refreshMessageCount(
            requireContext(),
            badge,
            "staff",
            getUserId(),
            staffType = getStaffType(),
            designation = getDesignation()
        )
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
