package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.saveetha.edualert.databinding.FragmentAdminHomeBinding

class AdminHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAdminHomeBinding.inflate(inflater, container, false)

        // Setup notification icon
        val notificationIconContainer = binding.root.findViewById<View>(R.id.notificationIconContainer)
        val notificationBadge = binding.root.findViewById<TextView>(R.id.notificationBadge)
        val userType = "admin"
        val userId = getUserId()
        
        NotificationManager.setupNotificationIcon(
            requireContext(),
            notificationIconContainer,
            notificationBadge,
            userType,
            userId
        )

        // Navigate to Student Message Fragment
        binding.messageStudentsCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, AdminStudent())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to Staff Message Fragment
        binding.messageStaffCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, AdminStaf())
                .addToBackStack(null)
                .commit()
        }

        // Navigate to Everyone Message Fragment
        binding.messageAllCard.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, Admineveryone())
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }
    
    private fun getUserId(): String {
        return UserSession.getUserId(requireContext()) ?: "ADM001"
    }
}
