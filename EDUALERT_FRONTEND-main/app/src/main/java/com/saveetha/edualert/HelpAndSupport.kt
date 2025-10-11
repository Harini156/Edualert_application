package com.saveetha.edualert

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class HelpSupportFragment : Fragment(R.layout.fragment_help_and_support) { // Use your layout name

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageView>(R.id.backButton)
        val emailSupport = view.findViewById<TextView>(R.id.emailSupport)

        // Back button navigates to AdminSettingsFragment
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        // Open email app when clicking the email
        emailSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:edualert.notifications@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Support Request")
            }
            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            }
        }
    }
}
