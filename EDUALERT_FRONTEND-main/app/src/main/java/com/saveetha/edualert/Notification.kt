package com.saveetha.edualert
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class NotificationSettingsActivity : AppCompatActivity() {

    private lateinit var pushSwitch: Switch
    private lateinit var emailSwitch: Switch
    private lateinit var vibrationSwitch: Switch
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)  // Replace with your XML file name

        // Initialize SharedPreferences
        sharedPrefs = getSharedPreferences("NotificationPrefs", MODE_PRIVATE)

        // Find views
        pushSwitch = findViewById(R.id.pushSwitch)
        emailSwitch = findViewById(R.id.emailSwitch)
        vibrationSwitch = findViewById(R.id.vibrationSwitch)

        // Load saved states
        pushSwitch.isChecked = sharedPrefs.getBoolean("push_notifications", false)
        emailSwitch.isChecked = sharedPrefs.getBoolean("email_updates", false)
        vibrationSwitch.isChecked = sharedPrefs.getBoolean("vibration_messages", false)

        // Handle switch changes
        pushSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("push_notifications", isChecked)
        }

        emailSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("email_updates", isChecked)
        }

        vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            savePreference("vibration_messages", isChecked)
        }
    }

    private fun savePreference(key: String, value: Boolean) {
        val editor = sharedPrefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }
}
