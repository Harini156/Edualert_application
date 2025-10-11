package com.saveetha.edualert

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.edualert.student.StudentProfileFragment

class StudentNavActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private var studentType: String? = null  // Can be used if needed

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_nav)

        bottomNav = findViewById(R.id.bottomNavigation)
        studentType = intent.getStringExtra("student_type")

        // Load default fragment (Home)
        loadFragment(StudentHomeFragment())

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(StudentHomeFragment())
                    true
                }
                R.id.nav_profile -> {
                    loadFragment(StudentProfileFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(StaffSettingsFragment())
                    true
                }
                else -> false
            }
        }

        // Update BottomNav visibility on back stack changes
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            bottomNav.visibility = when (fragment) {
                is ChangePasswordFragment,
                is HelpSupportFragment,
                is TermsConditionsFragment -> View.GONE
                else -> View.VISIBLE
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
