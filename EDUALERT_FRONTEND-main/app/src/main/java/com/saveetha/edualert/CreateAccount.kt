package com.saveetha.edualert

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreateAccount : AppCompatActivity() {

    private lateinit var nameField: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var cpasswordField: EditText
    private lateinit var departmentFieldStudent: EditText
    private lateinit var yearField: EditText
    private lateinit var departmentFieldStaff: EditText
    private lateinit var studentExtraLayout: LinearLayout
    private lateinit var staffExtraLayout: LinearLayout
    private lateinit var roleTabLayout: TabLayout
    private lateinit var createButton: Button
    private lateinit var togglePassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView

    private var isPasswordVisible = false
    private var isCPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        // Initialize views
        nameField = findViewById(R.id.fullNameField)
        emailField = findViewById(R.id.emailField)
        passwordField = findViewById(R.id.passwordField)
        cpasswordField = findViewById(R.id.confirmPasswordField)
        departmentFieldStudent = findViewById(R.id.departmentFieldStudent)
        yearField = findViewById(R.id.yearField)
        departmentFieldStaff = findViewById(R.id.departmentFieldStaff)
        studentExtraLayout = findViewById(R.id.studentExtraFields)
        staffExtraLayout = findViewById(R.id.staffExtraFields)
        roleTabLayout = findViewById(R.id.roleTabLayout)
        createButton = findViewById(R.id.createAccountButton)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)

        // Tabs: Student, Staff, Admin
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Student"))
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Staff"))
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Admin"))
        roleTabLayout.getTabAt(0)?.select()
        studentExtraLayout.visibility = View.VISIBLE
        staffExtraLayout.visibility = View.GONE

        // Toggle password visibility
        togglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            passwordField.inputType =
                if (isPasswordVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordField.setSelection(passwordField.text.length)
        }

        toggleConfirmPassword.setOnClickListener {
            isCPasswordVisible = !isCPasswordVisible
            cpasswordField.inputType =
                if (isCPasswordVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            cpasswordField.setSelection(cpasswordField.text.length)
        }

        // Update layouts on tab selection
        roleTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedRole = tab?.text.toString().lowercase()
                studentExtraLayout.visibility = if (selectedRole == "student") View.VISIBLE else View.GONE
                staffExtraLayout.visibility = if (selectedRole == "staff") View.VISIBLE else View.GONE
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Create account button
        createButton.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val currentTab = roleTabLayout.getTabAt(roleTabLayout.selectedTabPosition)
        val userType = currentTab?.text.toString().lowercase()

        val name = nameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString()
        val cpassword = cpasswordField.text.toString()
        val departmentStudent = departmentFieldStudent.text.toString().trim()
        val year = yearField.text.toString().trim()
        val departmentStaff = departmentFieldStaff.text.toString().trim()

        // Validations
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != cpassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        if (userType == "student" && (departmentStudent.isEmpty() || year.isEmpty())) {
            Toast.makeText(this, "Department and Year required for students", Toast.LENGTH_SHORT).show()
            return
        }
        // Staff department is now optional for non-teaching staff → no check

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating account...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val call = ApiClient.instance.registerUser(
            name,
            email,
            password,
            cpassword,
            userType,
            if (userType == "student") departmentStudent else if (userType == "staff") departmentStaff else "",
            if (userType == "student") year else ""
        )

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                progressDialog.dismiss()
                val result = response.body()
                if (response.isSuccessful && result?.status == "success") {
                    Toast.makeText(this@CreateAccount, "Account created successfully", Toast.LENGTH_SHORT).show()

                    // Directly go to Details page
                    val intent = Intent(this@CreateAccount, Details::class.java)
                    intent.putExtra("userType", userType)
                    intent.putExtra("userId", result.user_id)
                    intent.putExtra("isEdit", false)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@CreateAccount, result?.message ?: "Error occurred", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                progressDialog.dismiss()
                Toast.makeText(this@CreateAccount, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
