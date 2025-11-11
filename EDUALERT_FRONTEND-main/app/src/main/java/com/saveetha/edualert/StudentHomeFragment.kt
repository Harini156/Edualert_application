package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

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
    

}
