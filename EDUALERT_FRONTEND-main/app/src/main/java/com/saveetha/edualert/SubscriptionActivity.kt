package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class SubscriptionActivity : AppCompatActivity() {

    private lateinit var btnSubscribe: MaterialButton
    private lateinit var btnMaybeLater: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)

        // Initialize views
        btnSubscribe = findViewById(R.id.btnSubscribe)
        btnMaybeLater = findViewById(R.id.btnMaybeLater)

        // Subscribe button - Dummy (no action)
        btnSubscribe.setOnClickListener {
            // TODO: Add subscription logic here in future
            // For now, it's just a dummy button
        }

        // Maybe Later button - Navigate to Login
        btnMaybeLater.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
    }
}
