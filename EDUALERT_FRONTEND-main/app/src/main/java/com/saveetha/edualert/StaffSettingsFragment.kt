package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment

class StaffSettingsFragment : Fragment() {

    private var fromHod: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fromHod = arguments?.getBoolean("FROM_HOD", false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff_settings, container, false)

        // 🔙 Back Button
        val backButton: ImageView = view.findViewById(R.id.backButton)
        backButton.setOnClickListener {
            val homeFragment = if (fromHod) StaffHodFragment() else StaffFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, homeFragment)
                .addToBackStack(null)
                .commit()
        }

        // 🔑 Change Password
        val changePassword: LinearLayout = view.findViewById(R.id.changePasswordLayout)
        changePassword.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, ChangePasswordFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🆘 Help & Support
        val helpSupport: LinearLayout = view.findViewById(R.id.helpSupportLayout)
        helpSupport.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HelpSupportFragment())
                .addToBackStack(null)
                .commit()
        }

        // 📜 Terms & Conditions
        val termsConditions: LinearLayout = view.findViewById(R.id.termsLayout)
        termsConditions.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TermsConditionsFragment())
                .addToBackStack(null)
                .commit()
        }

        // 🚪 Logout
        val logout: LinearLayout = view.findViewById(R.id.logoutLayout)
        logout.setOnClickListener {
            // ✅ Clear UserSession on logout
            com.saveetha.edualert.UserSession.clearSession(requireContext())
            
            val intent = Intent(requireContext(), Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("LOGOUT_MSG", true)
            startActivity(intent)
        }

        return view
    }
}
