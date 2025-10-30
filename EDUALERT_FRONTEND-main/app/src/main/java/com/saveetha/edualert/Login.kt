package com.saveetha.edualert

import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
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
                        
                        // Enhanced error handling with debug info
                        val errorMessage = when {
                            t is com.google.gson.JsonSyntaxException -> 
                                "Server returned invalid JSON. This usually means there's a PHP error on the server."
                            t.message?.contains("ConnectException") == true -> 
                                "Cannot connect to server. Check if server is running and URL is correct."
                            t.message?.contains("SocketTimeoutException") == true -> 
                                "Server is taking too long to respond. Try again."
                            else -> "Network error: ${t.message}"
                        }
                        
                        Toast.makeText(this@Login, errorMessage, Toast.LENGTH_LONG).show()
                        
                        // Automatic debug info collection
                        val debugInfo = StringBuilder()
                        debugInfo.append("=== LOGIN ERROR DEBUG ===\n")
                        debugInfo.append("Timestamp: ${System.currentTimeMillis()}\n")
                        debugInfo.append("Error Type: ${t.javaClass.simpleName}\n")
                        debugInfo.append("Error Message: ${t.message}\n")
                        debugInfo.append("URL: ${call.request().url}\n")
                        debugInfo.append("Base URL: ${ApiClient.BASE_URL}\n")
                        debugInfo.append("Cause: ${t.cause?.message ?: "None"}\n")
                        
                        if (t is com.google.gson.JsonSyntaxException) {
                            debugInfo.append("\n=== JSON ERROR ANALYSIS ===\n")
                            debugInfo.append("The server is returning HTML/PHP errors instead of JSON.\n")
                            debugInfo.append("This usually means:\n")
                            debugInfo.append("1. PHP syntax errors in login.php\n")
                            debugInfo.append("2. Database connection issues\n")
                            debugInfo.append("3. Missing files after server update\n")
                        }
                        
                        debugInfo.append("\n=== STACK TRACE ===\n")
                        debugInfo.append(t.stackTraceToString())
                        
                        // Show detailed error dialog for debugging
                        showErrorWithCopy("Login Error - Auto Debug", debugInfo.toString())
                    }
                })
        }
        
        // Add debug button for login issues
        addDebugButton()
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
    
    private fun addDebugButton() {
        try {
            // Create debug button
            val debugButton = MaterialButton(this)
            debugButton.text = "🔧 LOGIN DEBUG"
            debugButton.setBackgroundColor(Color.parseColor("#FF5722"))
            debugButton.setTextColor(Color.WHITE)
            debugButton.textSize = 12f
            
            // Set button click listener
            debugButton.setOnClickListener {
                testLoginConnection()
            }
            
            // Find the main layout and add debug button - use a safer approach
            val contentView = findViewById<android.view.ViewGroup>(android.R.id.content)
            val rootView = contentView?.getChildAt(0) as? android.view.ViewGroup
            
            if (rootView != null) {
                // Create a wrapper layout if needed
                val layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
                )
                debugButton.layoutParams = layoutParams
                
                // Add to the root view
                rootView.addView(debugButton)
                Toast.makeText(this, "Debug button added - tap to test connection", Toast.LENGTH_SHORT).show()
            } else {
                // Fallback: show debug info in toast
                Toast.makeText(this, "Debug available: Tap login to see detailed errors", Toast.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            showErrorWithCopy("Debug Button Error", "Error: ${e.message}\nStack: ${e.stackTraceToString()}")
        }
    }
    
    private fun testLoginConnection() {
        val debugInfo = StringBuilder()
        debugInfo.append("=== LOGIN DEBUG ANALYSIS ===\n")
        debugInfo.append("Timestamp: ${System.currentTimeMillis()}\n")
        debugInfo.append("Base URL: ${ApiClient.BASE_URL}\n")
        debugInfo.append("Login Endpoint: ${ApiClient.BASE_URL}api/login.php\n\n")
        
        // Get actual form data
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val actualEmail = emailField.text.toString().trim()
        val actualPassword = passwordField.text.toString().trim()
        
        debugInfo.append("=== FORM DATA ANALYSIS ===\n")
        debugInfo.append("Email field content: '$actualEmail'\n")
        debugInfo.append("Email field length: ${actualEmail.length}\n")
        debugInfo.append("Password field content: '${if (actualPassword.isEmpty()) "EMPTY" else "***PROVIDED***"}'\n")
        debugInfo.append("Password field length: ${actualPassword.length}\n")
        debugInfo.append("Selected role: $selectedRole\n\n")
        
        // Test with actual form data
        debugInfo.append("Testing with ACTUAL form data...\n\n")
        
        ApiClient.instance.loginUser(actualEmail, actualPassword, selectedRole.lowercase())
            .enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    debugInfo.append("=== SERVER RESPONSE ===\n")
                    debugInfo.append("Response Code: ${response.code()}\n")
                    debugInfo.append("Response Message: ${response.message()}\n")
                    debugInfo.append("Is Successful: ${response.isSuccessful}\n")
                    
                    if (response.isSuccessful) {
                        debugInfo.append("Response Body: ${response.body()}\n")
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "No error body"
                        debugInfo.append("Error Body: $errorBody\n")
                    }
                    
                    debugInfo.append("\n=== RAW RESPONSE HEADERS ===\n")
                    response.headers().forEach { (name, value) ->
                        debugInfo.append("$name: $value\n")
                    }
                    
                    showErrorWithCopy("Login Debug Results", debugInfo.toString())
                }
                
                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    debugInfo.append("=== CONNECTION FAILURE ===\n")
                    debugInfo.append("Error Type: ${t.javaClass.simpleName}\n")
                    debugInfo.append("Error Message: ${t.message}\n")
                    debugInfo.append("Cause: ${t.cause?.message ?: "None"}\n")
                    
                    if (t is com.google.gson.JsonSyntaxException) {
                        debugInfo.append("\n=== JSON PARSING ERROR ===\n")
                        debugInfo.append("This indicates the server is returning non-JSON content\n")
                        debugInfo.append("Possible causes:\n")
                        debugInfo.append("1. PHP errors being output before JSON\n")
                        debugInfo.append("2. HTML error pages instead of JSON\n")
                        debugInfo.append("3. Server configuration issues\n")
                    }
                    
                    debugInfo.append("\n=== STACK TRACE ===\n")
                    debugInfo.append(t.stackTraceToString())
                    
                    showErrorWithCopy("Login Connection Error", debugInfo.toString())
                }
            })
    }
    
    private fun showErrorWithCopy(title: String, errorMessage: String) {
        val alertDialog = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(errorMessage)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Copy to Clipboard") { _, _ ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Login Debug Info", "$title\n\n$errorMessage")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "Debug info copied to clipboard!", Toast.LENGTH_SHORT).show()
            }
            .create()
        
        alertDialog.show()
    }
}
