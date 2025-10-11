package com.saveetha.edualert
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class TermsConditionsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_terms_and_cond, container, false)

        // Back Button click → Go to AdminSettingsFragment
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }


        // Email Support click → Open Gmail
        val emailSupport: TextView = view.findViewById(R.id.emailSupport)
        emailSupport.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:") // only email apps handle this
                putExtra(Intent.EXTRA_EMAIL, arrayOf("support@edualertapp.com"))
                putExtra(Intent.EXTRA_SUBJECT, "EduAlert Support")
                putExtra(Intent.EXTRA_TEXT, "Hello EduAlert Team,\n\nI need help with...")
            }

            // Prefer Gmail if installed
            emailIntent.`package` = "com.google.android.gm"

            try {
                startActivity(emailIntent)
            } catch (e: Exception) {
                // If Gmail not installed, open chooser
                val chooser = Intent.createChooser(emailIntent, "Send Email")
                startActivity(chooser)
            }
        }

        return view
    }
}
