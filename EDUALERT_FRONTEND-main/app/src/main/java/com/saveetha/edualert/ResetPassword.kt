package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var otpLayout: LinearLayout
    private lateinit var otpField: EditText
    private lateinit var newPasswordField: EditText
    private lateinit var sendOtpButton: AppCompatButton
    private lateinit var resetPasswordButton: AppCompatButton
    private lateinit var loginLink: TextView // Added login link

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        // Initialize views
        emailField = findViewById(R.id.emailField)
        otpLayout = findViewById(R.id.otpLayout)
        otpField = findViewById(R.id.otpField)
        newPasswordField = findViewById(R.id.newPasswordField)
        sendOtpButton = findViewById(R.id.sendOtpButton)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        loginLink = findViewById(R.id.loginLink) // Initialize login TextView

        // Click Send OTP
        sendOtpButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendOtp(email)
        }

        // Click Reset Password
        resetPasswordButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val otp = otpField.text.toString().trim()
            val newPass = newPasswordField.text.toString().trim()

            if (otp.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "OTP and new password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetPassword(email, otp, newPass)
        }

        // Click Login link → navigate to Login activity
        loginLink.setOnClickListener {
            startActivity(Intent(this@ResetPasswordActivity, Login::class.java))
            finish()
        }
    }

    private fun sendOtp(email: String) {
        ApiClient.instance.sendOtp(email).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@ResetPasswordActivity, "OTP sent successfully", Toast.LENGTH_SHORT).show()
                    otpLayout.visibility = View.VISIBLE
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Error: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ResetPasswordActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun resetPassword(email: String, otp: String, newPassword: String) {
        ApiClient.instance.resetPassword(email, otp, newPassword).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@ResetPasswordActivity, "Password reset successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to login screen
                    startActivity(Intent(this@ResetPasswordActivity, Login::class.java))
                    finish()
                } else {
                    Toast.makeText(this@ResetPasswordActivity, "Error: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@ResetPasswordActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
