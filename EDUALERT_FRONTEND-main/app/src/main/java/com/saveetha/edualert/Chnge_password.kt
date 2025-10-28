package com.saveetha.edualert

import android.content.Context
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
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

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
        val url = ApiClient.BASE_URL + "api/change_password.php"
        val requestQueue = Volley.newRequestQueue(requireContext())

        // ✅ Get email from UserSession
        val email = com.saveetha.edualert.UserSession.getEmail(requireContext())

        if (email.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Email not found in session", Toast.LENGTH_SHORT).show()
            return
        }

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    val status = json.getString("status")
                    val message = json.getString("message")

                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                    if (status == "success") {
                        // ✅ Show success toast
                        Toast.makeText(requireContext(), "Password updated successfully", Toast.LENGTH_SHORT).show()

                        // ✅ Clear fields
                        oldPasswordField.text.clear()
                        newPasswordField.text.clear()
                        confirmNewPasswordField.text.clear()

                        // ✅ Navigate to AdminSettingsFragment
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, AdminSettingsFragment()) // Correct container ID
                            .addToBackStack(null)
                            .commit()
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
                val params = mutableMapOf<String, String>()
                params["email"] = email   // ✅ Taken from SharedPreferences
                params["old_password"] = oldPassword
                params["new_password"] = newPassword
                return params
            }
        }

        requestQueue.add(stringRequest)
    }
}
