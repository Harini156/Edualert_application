package com.saveetha.edualert

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Login : AppCompatActivity() {

    private var isPasswordVisible = false
    private var selectedRole = "Student"  // default role set to Student

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val tabLayout = findViewById<TabLayout>(R.id.roleTabLayout)
        val roleIcon = findViewById<ImageView>(R.id.roleIcon)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val togglePassword = findViewById<ImageView>(R.id.togglePassword)
        val signupText = findViewById<TextView>(R.id.signUpLink)
        val loginButton = findViewById<MaterialButton>(R.id.loginButton)
        val forgotPassword = findViewById<TextView>(R.id.forgotPassword)

        if (intent.getBooleanExtra("LOGOUT_MSG", false)) {
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        }

        // 🔹 Student + Staff + Admin Tabs
        tabLayout.addTab(tabLayout.newTab().setText("Student"))
        tabLayout.addTab(tabLayout.newTab().setText("Staff"))
        tabLayout.addTab(tabLayout.newTab().setText("Admin"))

        roleIcon.setImageResource(R.drawable.stuframe)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedRole = tab.text.toString()
                when (tab.position) {
                    0 -> roleIcon.setImageResource(R.drawable.stuframe)
                    1 -> roleIcon.setImageResource(R.drawable.stafframe)
                    2 -> roleIcon.setImageResource(R.drawable.adminframe)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Toggle password visibility
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordField.inputType = if (isPasswordVisible)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordField.setSelection(passwordField.text.length)
        }

        signupText.setOnClickListener {
            startActivity(Intent(this, CreateAccount::class.java))
        }

        forgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // 🔹 Login button
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val role = selectedRole.lowercase()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both Email and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Logging in...")
            progressDialog.setCancelable(false)
            progressDialog.show()
            loginButton.isEnabled = false

            ApiClient.instance.loginUser(email, password, role)
                .enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(
                        call: Call<LoginResponse>,
                        response: Response<LoginResponse>
                    ) {
                        progressDialog.dismiss()
                        loginButton.isEnabled = true

                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.status == "success") {
                                val user = body.user
                                val userType = user.user_type.lowercase()
                                val userId = user.user_id

                                // ✅ Save basic user session data
                                UserSession.saveUserSession(
                                    context = this@Login,
                                    userId = userId,
                                    userType = userType,
                                    name = user.name,
                                    email = user.email
                                )

                                // 🔹 Open dashboard immediately
                                when (userType) {
                                    "admin" -> {
                                        startActivity(Intent(this@Login, AdminNavActivity::class.java))
                                        finish()
                                    }
                                    "staff" -> {
                                        fetchStaffDetails(userId, progressDialog)
                                    }
                                    "student" -> {
                                        fetchStudentDetails(userId, progressDialog)
                                    }
                                    else -> {
                                        Toast.makeText(this@Login, "Unknown role: $userType", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this@Login, body.message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@Login, "Login failed. Try again.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        progressDialog.dismiss()
                        loginButton.isEnabled = true
                        
                        Toast.makeText(this@Login, "Login failed. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                })
        }
        

    }

    // ------------------------
    // Staff Details
    // ------------------------
    private fun fetchStaffDetails(userId: String, progressDialog: ProgressDialog) {
        ApiClient.instance.getStaffDetails(userId)
            .enqueue(object : Callback<StaffDetailsResponse> {
                override fun onResponse(
                    call: Call<StaffDetailsResponse>,
                    response: Response<StaffDetailsResponse>
                ) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        val staff = response.body()!!.data
                        if (staff != null) {
                            val designation = staff.designation?.lowercase() ?: ""
                            val staffType = staff.staff_type?.lowercase() ?: ""

                            // ✅ Update UserSession with staff details
                            UserSession.saveUserSession(
                                context = this@Login,
                                userId = userId,
                                userType = "staff",
                                name = UserSession.getName(this@Login) ?: "",
                                email = UserSession.getEmail(this@Login) ?: "",
                                department = staff.department,
                                staffType = staff.staff_type,
                                designation = staff.designation
                            )

                            val intent = Intent(this@Login, StaffNavActivity::class.java)
                            intent.putExtra("designation", designation)
                            intent.putExtra("staff_type", staffType)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "No staff data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Login, "Failed to fetch staff details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StaffDetailsResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@Login, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ------------------------
    // Student Details
    // ------------------------
    private fun fetchStudentDetails(userId: String, progressDialog: ProgressDialog) {
        ApiClient.instance.getStudentDetails(userId)
            .enqueue(object : Callback<StudentDetailsResponse> {
                override fun onResponse(
                    call: Call<StudentDetailsResponse>,
                    response: Response<StudentDetailsResponse>
                ) {
                    progressDialog.dismiss()
                    if (response.isSuccessful && response.body() != null) {
                        val student = response.body()!!.data
                        if (student != null) {
                            // ✅ Update UserSession with complete student details
                            UserSession.saveUserSession(
                                context = this@Login,
                                userId = userId,
                                userType = "student",
                                name = UserSession.getName(this@Login) ?: "",
                                email = UserSession.getEmail(this@Login) ?: "",
                                department = student.department,
                                year = student.year,
                                bloodGroup = student.blood_group,
                                phone = student.phone,
                                gender = student.gender,
                                dob = student.dob,
                                cgpa = student.cgpa,
                                backlogs = student.backlogs,
                                stayType = student.stay_type,
                                address = student.address
                            )

                            val intent = Intent(this@Login, StudentNavActivity::class.java)
                            intent.putExtra("user_id", student.user_id)
                            intent.putExtra("department", student.department)
                            intent.putExtra("year", student.year)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@Login, "No student data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Login, "Failed to fetch student details", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<StudentDetailsResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    Toast.makeText(this@Login, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
    

}
