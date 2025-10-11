package com.saveetha.edualert

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.saveetha.edualert.staff.StaffProfileFragment

class StaffNavActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_nav)

        bottomNav = findViewById(R.id.bottomNavigation)

        // Listen to back stack changes to update BottomNav visibility
        supportFragmentManager.addOnBackStackChangedListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            updateBottomNavVisibility(fragment)
        }

        val designation = intent.getStringExtra("designation") ?: ""
        val staffType = intent.getStringExtra("staff_type") ?: ""

        // Load default fragment
        if (designation == "hod") loadFragment(StaffHodFragment())
        else loadFragment(StaffFragment().apply {
            arguments = Bundle().apply { putString("staff_type", staffType) }
        })

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    if (designation == "hod") loadFragment(StaffHodFragment())
                    else loadFragment(StaffFragment().apply {
                        arguments = Bundle().apply { putString("staff_type", staffType) }
                    })
                    true
                }

                R.id.nav_profile -> {
                    loadFragment(StaffProfileFragment())
                    true
                }

                R.id.nav_settings -> {
                    loadFragment(StaffSettingsFragment())
                    true
                }

                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null) // important to remember history
            .commit()

        updateBottomNavVisibility(fragment)
    }

    private fun updateBottomNavVisibility(fragment: Fragment?) {
        bottomNav.visibility = when (fragment) {
            is ChangePasswordFragment,
            is HelpSupportFragment,
            is TermsConditionsFragment -> View.GONE

            else -> View.VISIBLE
        }
    }
}