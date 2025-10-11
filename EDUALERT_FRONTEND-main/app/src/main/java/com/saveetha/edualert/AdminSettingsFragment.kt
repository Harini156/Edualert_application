package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class AdminSettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_settings, container, false)

        // 🔙 Back Arrow → AdminHomeFragment
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, AdminHomeFragment())
                .addToBackStack(null)
                .commit()
        }

        // 👤 My Profile → ProfileAdmin
        val myProfile: LinearLayout = view.findViewById(R.id.myProfileLayout)
        myProfile.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, ProfileAdmin())
                .addToBackStack(null)
                .commit()
        }

        // 🔑 Change Password → ChangePasswordFragment
        val changePassword: LinearLayout = view.findViewById(R.id.changePasswordLayout)
        changePassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🆘 Help & Support → HelpSupportFragment
        val helpSupport: LinearLayout = view.findViewById(R.id.helpSupportLayout)
        helpSupport.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, HelpSupportFragment())
                .addToBackStack(null)
                .commit()
        }

        // 📜 Terms & Conditions → TermsConditionsFragment
        val termsConditions: LinearLayout = view.findViewById(R.id.termsLayout)
        termsConditions.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, TermsConditionsFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🚪 Logout → LoginActivity + Pass flag
        val logout: LinearLayout = view.findViewById(R.id.logoutLayout)
        logout.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("LOGOUT_MSG", true) // ✅ pass flag
            startActivity(intent)
        }

        return view
    }
}
