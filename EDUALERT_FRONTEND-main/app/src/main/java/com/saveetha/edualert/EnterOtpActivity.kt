package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EnterOtpActivity : AppCompatActivity() {

    private lateinit var emailField: EditText
    private lateinit var otpField: EditText
    private lateinit var newPasswordField: EditText
    private lateinit var resetButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_otp)

        emailField = findViewById(R.id.emailField)
        otpField = findViewById(R.id.otpField)
        newPasswordField = findViewById(R.id.newPasswordField)
        resetButton = findViewById(R.id.resetPasswordButton)

        // Get email from previous screen
        val email = intent.getStringExtra("email")
        emailField.setText(email)

        resetButton.setOnClickListener {
            val otp = otpField.text.toString()
            val newPass = newPasswordField.text.toString()

            if (otp.isEmpty() || newPass.isEmpty()) {
                Toast.makeText(this, "OTP and password are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            resetPassword(email ?: "", otp, newPass)
        }
    }

    private fun resetPassword(email: String, otp: String, newPassword: String) {
        ApiClient.instance.resetPassword(email, otp, newPassword)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@EnterOtpActivity, "Password reset successfully", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@EnterOtpActivity, Login::class.java))
                        finish()
                    } else {
                        Toast.makeText(this@EnterOtpActivity, "Error: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@EnterOtpActivity, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
