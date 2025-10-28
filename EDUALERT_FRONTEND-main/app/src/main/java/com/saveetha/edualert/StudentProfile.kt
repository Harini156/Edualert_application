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
import com.saveetha.edualert.UserSession
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

        // ✅ Use UserSession instead of SharedPreferences
        studentId = UserSession.getUserId(requireContext()) ?: ""
        studentEmail = UserSession.getEmail(requireContext()) ?: ""

        if (studentId.isEmpty() && studentEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Student details not found. Please login again.", Toast.LENGTH_LONG).show()
            return view
        }

        // Load cached values from UserSession
        tvStudentId.setText(studentId)
        tvEmail.setText(studentEmail)
        tvDepartment.setText(UserSession.getDepartment(requireContext()) ?: "")
        tvYear.setText(UserSession.getYear(requireContext()) ?: "")
        tvBloodGroup.setText(UserSession.getBloodGroup(requireContext()) ?: "")
        tvPhone.setText(UserSession.getPhone(requireContext()) ?: "")
        tvGender.setText(UserSession.getGender(requireContext()) ?: "")

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

                        // ✅ Update UserSession with complete student data
                        UserSession.saveUserSession(
                            context = requireContext(),
                            userId = studentId,
                            userType = "student",
                            name = UserSession.getName(requireContext()) ?: "",
                            email = studentEmail,
                            department = student.optString("department", ""),
                            year = student.optString("year", ""),
                            bloodGroup = student.optString("blood_group", ""),
                            phone = student.optString("phone", ""),
                            gender = gender
                        )
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
