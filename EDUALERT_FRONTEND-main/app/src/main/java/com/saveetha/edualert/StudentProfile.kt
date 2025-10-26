package com.saveetha.edualert.student

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.saveetha.edualert.ApiClient
import com.saveetha.edualert.Details
import com.saveetha.edualert.R
import org.json.JSONObject

class StudentProfileFragment : Fragment() {

    private lateinit var tvStudentId: EditText
    private lateinit var tvEmail: EditText
    private lateinit var tvDepartment: EditText
    private lateinit var tvYear: EditText
    private lateinit var tvBloodGroup: EditText
    private lateinit var tvPhone: EditText
    private lateinit var tvGender: EditText   // ✅ Gender field
    private lateinit var btnEditDetails: Button

    private lateinit var sharedPref: android.content.SharedPreferences

    private var studentId: String = ""
    private var studentEmail: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_profile, container, false)

        // Initialize Views
        tvStudentId = view.findViewById(R.id.tvStudentId)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvDepartment = view.findViewById(R.id.tvDepartment)
        tvYear = view.findViewById(R.id.tvYear)
        tvBloodGroup = view.findViewById(R.id.tvBloodGroup)
        tvPhone = view.findViewById(R.id.tvPhone)
        tvGender = view.findViewById(R.id.tvGender)
        btnEditDetails = view.findViewById(R.id.btnEditDetails)

        // SharedPreferences
        sharedPref = requireContext().getSharedPreferences("EduAlertPrefs", Context.MODE_PRIVATE)
        studentId = sharedPref.getString("USER_ID", "") ?: ""
        studentEmail = sharedPref.getString("EMAIL", "") ?: ""

        if (studentId.isEmpty() && studentEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Student details not found. Please login again.", Toast.LENGTH_LONG).show()
            return view
        }

        // Load cached values
        tvStudentId.setText(studentId)
        tvEmail.setText(studentEmail)
        tvDepartment.setText(sharedPref.getString("DEPARTMENT", "") ?: "")
        tvYear.setText(sharedPref.getString("YEAR", "") ?: "")
        tvBloodGroup.setText(sharedPref.getString("BLOOD_GROUP", "") ?: "")
        tvPhone.setText(sharedPref.getString("PHONE", "") ?: "")
        tvGender.setText(sharedPref.getString("GENDER", "") ?: "")

        // Fetch latest details from server
        fetchStudentProfile(studentId, studentEmail)

        // Edit button click
        btnEditDetails.setOnClickListener {
            val intent = Intent(requireContext(), Details::class.java)
            intent.putExtra("userType", "student")
            intent.putExtra("userId", studentId)
            intent.putExtra("isEditMode", true)
            startActivity(intent)
        }

        return view
    }

    private fun fetchStudentProfile(studentId: String, email: String) {
        val url = ApiClient.BASE_URL + "api/studentprofile.php"
        val requestQueue = Volley.newRequestQueue(requireContext())

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getString("status") == "success") {
                        val student = json.getJSONObject("student")

                        // ✅ Capitalize Gender properly
                        val genderRaw = student.optString("gender", "").lowercase()
                        val gender = genderRaw.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }

                        // Update UI
                        tvStudentId.setText(student.getString("user_id"))
                        tvEmail.setText(student.getString("email"))
                        tvDepartment.setText(student.optString("department", "")) // ✅ fixed
                        tvYear.setText(student.optString("year", ""))
                        tvBloodGroup.setText(student.optString("blood_group", ""))
                        tvPhone.setText(student.optString("phone", ""))
                        tvGender.setText(gender)  // ✅ capitalized

                        // Save to SharedPreferences
                        sharedPref.edit()
                            .putString("DEPARTMENT", student.optString("department", ""))
                            .putString("YEAR", student.optString("year", ""))
                            .putString("BLOOD_GROUP", student.optString("blood_group", ""))
                            .putString("PHONE", student.optString("phone", ""))
                            .putString("GENDER", gender)
                            .apply()
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
                val params = HashMap<String, String>()
                if (studentId.isNotEmpty()) params["student_id"] = studentId
                if (email.isNotEmpty()) params["email"] = email
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
