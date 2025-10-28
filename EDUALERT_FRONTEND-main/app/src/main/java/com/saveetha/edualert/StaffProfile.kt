package com.saveetha.edualert.staff

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.saveetha.edualert.ApiClient
import com.saveetha.edualert.Details
import com.saveetha.edualert.R
import com.saveetha.edualert.UserSession
import org.json.JSONObject

class StaffProfileFragment : Fragment() {

    private lateinit var tvStaffId: EditText
    private lateinit var tvEmail: EditText
    private lateinit var tvDepartment: EditText
    private lateinit var tvDesignation: EditText
    private lateinit var btnEditDetails: Button

    private lateinit var departmentLayout: LinearLayout
    private lateinit var designationLayout: LinearLayout

    private var staffUserId: String = ""
    private var staffEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_staff_profile, container, false)

        tvStaffId = view.findViewById(R.id.tvStaffId)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvDepartment = view.findViewById(R.id.tvDepartment)
        tvDesignation = view.findViewById(R.id.tvDesignation)
        btnEditDetails = view.findViewById(R.id.btnEditDetails)

        departmentLayout = view.findViewById(R.id.departmentLayout)
        designationLayout = view.findViewById(R.id.designationLayout)

        // ✅ Use UserSession instead of SharedPreferences
        staffUserId = UserSession.getUserId(requireContext()) ?: ""
        staffEmail = UserSession.getEmail(requireContext()) ?: ""

        if (staffUserId.isEmpty() && staffEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Staff details not found. Please login again.", Toast.LENGTH_LONG).show()
            return view
        }

        // Show cached values from UserSession
        tvStaffId.setText(staffUserId)
        tvEmail.setText(staffEmail)
        tvDepartment.setText(UserSession.getDepartment(requireContext()) ?: "")
        tvDesignation.setText(UserSession.getDesignation(requireContext()) ?: "")

        val cachedType = UserSession.getStaffType(requireContext())
        toggleFields(cachedType)

        fetchStaffProfile(staffUserId, staffEmail)

        btnEditDetails.setOnClickListener {
            val intent = Intent(requireContext(), Details::class.java)
            intent.putExtra("userType", "staff")
            intent.putExtra("userId", staffUserId)
            intent.putExtra("isEditMode", true)
            startActivity(intent)
        }

        return view
    }

    private fun fetchStaffProfile(staffId: String, email: String) {
        val ctx = context ?: return  // Safe check
        val url = ApiClient.BASE_URL + "api/staffprofile.php"
        val requestQueue = Volley.newRequestQueue(ctx)

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                // Safe execution: only proceed if fragment is added
                if (isAdded) {
                    try {
                        val json = JSONObject(response)
                        if (json.getString("status") == "success") {
                            val staff = json.getJSONObject("staff")
                            val staffType = staff.optString("staff_type", "")

                            tvStaffId.setText(staff.getString("user_id"))
                            tvEmail.setText(staff.getString("email"))
                            tvDepartment.setText(staff.optString("department", ""))
                            tvDesignation.setText(staff.optString("designation", ""))

                            toggleFields(staffType)

                            // ✅ Update UserSession with complete staff data
                            UserSession.saveUserSession(
                                context = ctx,
                                userId = staffUserId,
                                userType = "staff",
                                name = UserSession.getName(ctx) ?: "",
                                email = staffEmail,
                                department = staff.optString("department", ""),
                                staffType = staffType,
                                designation = staff.optString("designation", "")
                            )
                        } else {
                            Toast.makeText(ctx, json.getString("message"), Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(ctx, "Parsing error", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            { error ->
                if (isAdded) {
                    error.printStackTrace()
                    Toast.makeText(ctx, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                if (staffId.isNotEmpty()) params["staff_id"] = staffId
                if (email.isNotEmpty()) params["email"] = email
                return params
            }
        }

        requestQueue.add(stringRequest)
    }

    private fun toggleFields(staffType: String?) {
        val normalizedStaffType = staffType?.trim()?.lowercase()
        if (normalizedStaffType == "non-teaching") {
            departmentLayout.visibility = View.GONE
            designationLayout.visibility = View.GONE
        } else {
            departmentLayout.visibility = View.VISIBLE
            designationLayout.visibility = View.VISIBLE
        }
    }
}
