package com.saveetha.edualert

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class ProfileAdmin : Fragment() {

    private lateinit var tvAdminId: EditText
    private lateinit var tvEmail: EditText
    private lateinit var tvRole: TextView
    private lateinit var backButton: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_admin, container, false)

        tvAdminId = view.findViewById(R.id.tvAdminId)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvRole = view.findViewById(R.id.tvRole)
        backButton = view.findViewById(R.id.backButton)

        backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // ✅ Get logged-in admin details from UserSession
        val adminId = com.saveetha.edualert.UserSession.getUserId(requireContext()) ?: ""
        val email = com.saveetha.edualert.UserSession.getEmail(requireContext()) ?: ""

        if (adminId.isEmpty() || email.isEmpty()) {
            Toast.makeText(requireContext(), "Admin details not found. Please login again.", Toast.LENGTH_LONG).show()
            return view
        }

        Log.d("ProfileAdmin", "AdminId: $adminId, Email: $email")

        fetchAdminProfile(adminId, email)

        return view
    }

    private fun fetchAdminProfile(adminId: String, email: String) {
        val url = ApiClient.BASE_URL + "api/adminprofile.php"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                Log.d("ProfileAdmin", "Response: $response")

                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")
                    if (status == "success") {
                        val admin = json.getJSONObject("admin")
                        tvAdminId.setText(admin.getString("user_id"))
                        tvEmail.setText(admin.getString("email"))
                        tvRole.text = "Administrator"
                    } else {
                        Toast.makeText(requireContext(), json.getString("message"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Parsing error", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(requireContext(), "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = mutableMapOf<String, String>()
                if (adminId.isNotEmpty()) params["admin_id"] = adminId
                if (email.isNotEmpty()) params["email"] = email
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
