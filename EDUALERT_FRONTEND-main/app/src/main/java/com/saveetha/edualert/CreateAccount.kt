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
        roleTabLayout = findViewById(R.id.roleTabLayout)
        createButton = findViewById(R.id.createAccountButton)
        togglePassword = findViewById(R.id.togglePassword)
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword)

        // Tabs: Student, Staff, Admin
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Student"))
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Staff"))
        roleTabLayout.addTab(roleTabLayout.newTab().setText("Admin"))
        roleTabLayout.getTabAt(0)?.select()

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

        // No extra layouts needed - removed department/year fields

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

        // Validations - only basic fields now
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            return
        }
        if (password != cpassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Creating account...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val call = ApiClient.instance.registerUser(
            name,
            email,
            password,
            cpassword,
            userType
        )

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                progressDialog.dismiss()
                val result = response.body()
                if (response.isSuccessful && result?.status == "success") {
                    Toast.makeText(this@CreateAccount, "Account created successfully", Toast.LENGTH_SHORT).show()

                    // Go to Details page for extended information
                    val intent = Intent(this@CreateAccount, Details::class.java)
                    intent.putExtra("userType", userType)
                    intent.putExtra("userId", result.user_id)
                    intent.putExtra("isEditMode", false)
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
