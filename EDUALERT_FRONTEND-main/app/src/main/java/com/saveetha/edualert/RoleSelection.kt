package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RoleSelectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_role_selection)  // Your XML filename

        // Find the three cards (Student, Staff, Admin)
        val roleContainer = findViewById<LinearLayout>(R.id.roleContainer)
        val studentCard = roleContainer.getChildAt(0) as LinearLayout
        val staffCard = roleContainer.getChildAt(1) as LinearLayout
        val adminCard = roleContainer.getChildAt(2) as LinearLayout

        // Handle Student click
        studentCard.setOnClickListener {
            navigateToLogin("Student")
        }

        // Handle Staff click
        staffCard.setOnClickListener {
            navigateToLogin("Staff")
        }

        // Handle Admin click
        adminCard.setOnClickListener {
            navigateToLogin("Admin")
        }
    }

    // Function to navigate to Login screen with role info
    private fun navigateToLogin(role: String) {
        Toast.makeText(this, "$role selected", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, Login::class.java)
        intent.putExtra("ROLE", role)  // Send selected role to LoginActivity
        startActivity(intent)
    }
}
