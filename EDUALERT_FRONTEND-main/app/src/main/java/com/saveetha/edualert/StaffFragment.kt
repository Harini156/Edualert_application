package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.saveetha.edualert.AdminMessagesFragment
import com.saveetha.edualert.ReceivedMessagesFragment

class StaffFragment : Fragment() {

    private var staffType: String? = null  // Teaching / Non-Teaching

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff, container, false)

        val messagesFromAdminCard = view.findViewById<LinearLayout>(R.id.messagesFromAdminCard)
        val messagesFromHodCard = view.findViewById<LinearLayout>(R.id.messagesFromHodCard)

        // Get staff type from arguments
        staffType = arguments?.getString("staff_type", "Teaching")

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
}
