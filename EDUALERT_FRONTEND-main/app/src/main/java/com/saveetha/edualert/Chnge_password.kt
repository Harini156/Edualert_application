package com.saveetha.edualert

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChangePasswordFragment : Fragment() {

    private lateinit var oldPasswordField: EditText
    private lateinit var newPasswordField: EditText
    private lateinit var confirmNewPasswordField: EditText
    private lateinit var backButton: ImageView
    private lateinit var changePasswordButton: Button

    // 👁️ Toggle icons
    private lateinit var toggleOldPassword: ImageView
    private lateinit var toggleNewPassword: ImageView
    private lateinit var toggleConfirmPassword: ImageView

    // 👁️ State variables
    private var isOldVisible = false
    private var isNewVisible = false
    private var isConfirmVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chnge_password, container, false)

        // Bind views
        oldPasswordField = view.findViewById(R.id.oldPasswordField)
        newPasswordField = view.findViewById(R.id.newPasswordField)
        confirmNewPasswordField = view.findViewById(R.id.confirmNewPasswordField)
        backButton = view.findViewById(R.id.backButton)
        changePasswordButton = view.findViewById(R.id.changePasswordButton)

        toggleOldPassword = view.findViewById(R.id.toggleOldPassword)
        toggleNewPassword = view.findViewById(R.id.toggleNewPassword)
        toggleConfirmPassword = view.findViewById(R.id.toggleConfirmNewPassword)

        // 👁️ Eye toggle for old password
        toggleOldPassword.setOnClickListener {
            isOldVisible = !isOldVisible
            oldPasswordField.inputType =
                if (isOldVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            oldPasswordField.setSelection(oldPasswordField.text.length)
        }

        // 👁️ Eye toggle for new password
        toggleNewPassword.setOnClickListener {
            isNewVisible = !isNewVisible
            newPasswordField.inputType =
                if (isNewVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            newPasswordField.setSelection(newPasswordField.text.length)
        }

        // 👁️ Eye toggle for confirm password
        toggleConfirmPassword.setOnClickListener {
            isConfirmVisible = !isConfirmVisible
            confirmNewPasswordField.inputType =
                if (isConfirmVisible) InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                else InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            confirmNewPasswordField.setSelection(confirmNewPasswordField.text.length)
        }

        // Back arrow → go back
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Handle password update
        changePasswordButton.setOnClickListener {
            val oldPassword = oldPasswordField.text.toString().trim()
            val newPassword = newPasswordField.text.toString().trim()
            val confirmPassword = confirmNewPasswordField.text.toString().trim()

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (newPassword != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                changePassword(oldPassword, newPassword)
            }
        }

        return view
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        // Get email from UserSession
        val email = UserSession.getEmail(requireContext())

        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email not found in session", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable button to prevent multiple requests
        changePasswordButton.isEnabled = false
        changePasswordButton.text = "Changing..."

        // Use Retrofit API call
        ApiClient.instance.changePassword(email, oldPassword, newPassword)
            .enqueue(object : Callback<ChangePasswordResponse> {
                override fun onResponse(
                    call: Call<ChangePasswordResponse>,
                    response: Response<ChangePasswordResponse>
                ) {
                    // Re-enable button
                    changePasswordButton.isEnabled = true
                    changePasswordButton.text = "Change Password"

                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        
                        if (body.status == "success") {
                            Toast.makeText(requireContext(), "Password changed successfully!", Toast.LENGTH_LONG).show()
                            
                            // Clear fields
                            oldPasswordField.text.clear()
                            newPasswordField.text.clear()
                            confirmNewPasswordField.text.clear()
                            
                            // Go back to previous screen
                            requireActivity().supportFragmentManager.popBackStack()
                        } else {
                            Toast.makeText(requireContext(), body.message, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to change password. Please try again.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ChangePasswordResponse>, t: Throwable) {
                    // Re-enable button
                    changePasswordButton.isEnabled = true
                    changePasswordButton.text = "Change Password"
                    
                    Toast.makeText(requireContext(), "Network error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
    }
}
