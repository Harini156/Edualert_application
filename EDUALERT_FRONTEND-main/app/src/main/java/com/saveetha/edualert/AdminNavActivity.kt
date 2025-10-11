package com.saveetha.edualert

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminNavActivity : AppCompatActivity() {

    private var bottomNav: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_nav)

        bottomNav = findViewById(R.id.bottomNavigation)

        if (bottomNav == null) {
            Log.e("AdminNavActivity", "BottomNavigationView not found! Check XML layout.")
        }

        // Load Home fragment only if first launch
        if (savedInstanceState == null) {
            loadFragment(AdminHomeFragment(), false)
        }

        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(AdminHomeFragment())
                    true
                }
                R.id.nav_sent -> {
                    loadFragment(AdminSentFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(AdminSettingsFragment())
                    true
                }
                else -> false
            }
        }

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFragment =
                supportFragmentManager.findFragmentById(R.id.fragmentAdminContainer)
            updateBottomNavVisibility(currentFragment)
        }
    }

    private fun loadFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentAdminContainer, fragment)

        if (addToBackStack) transaction.addToBackStack(null)
        transaction.commitAllowingStateLoss() // safer to avoid IllegalStateException

        updateBottomNavVisibility(fragment)
    }

    private fun updateBottomNavVisibility(fragment: Fragment?) {
        bottomNav?.visibility = if (fragment is ProfileAdmin ||
            fragment is ChangePasswordFragment ||
            fragment is HelpSupportFragment ||
            fragment is TermsConditionsFragment
        ) View.GONE else View.VISIBLE
    }
}
