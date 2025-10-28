package com.saveetha.edualert

import android.content.Context
import android.content.SharedPreferences

object UserSession {
    private const val PREF_NAME = "EduAlertSession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_TYPE = "user_type"
    private const val KEY_NAME = "name"
    private const val KEY_EMAIL = "email"
    private const val KEY_DEPARTMENT = "department"
    private const val KEY_YEAR = "year"
    private const val KEY_STAFF_TYPE = "staff_type"
    private const val KEY_DESIGNATION = "designation"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    
    // Additional student fields
    private const val KEY_BLOOD_GROUP = "blood_group"
    private const val KEY_PHONE = "phone"
    private const val KEY_GENDER = "gender"
    private const val KEY_DOB = "dob"
    private const val KEY_CGPA = "cgpa"
    private const val KEY_BACKLOGS = "backlogs"
    private const val KEY_STAY_TYPE = "stay_type"
    private const val KEY_ADDRESS = "address"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserSession(
        context: Context,
        userId: String,
        userType: String,
        name: String,
        email: String,
        department: String? = null,
        year: String? = null,
        staffType: String? = null,
        designation: String? = null,
        // Additional student fields
        bloodGroup: String? = null,
        phone: String? = null,
        gender: String? = null,
        dob: String? = null,
        cgpa: String? = null,
        backlogs: String? = null,
        stayType: String? = null,
        address: String? = null
    ) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USER_TYPE, userType)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_DEPARTMENT, department)
        editor.putString(KEY_YEAR, year)
        editor.putString(KEY_STAFF_TYPE, staffType)
        editor.putString(KEY_DESIGNATION, designation)
        editor.putString(KEY_BLOOD_GROUP, bloodGroup)
        editor.putString(KEY_PHONE, phone)
        editor.putString(KEY_GENDER, gender)
        editor.putString(KEY_DOB, dob)
        editor.putString(KEY_CGPA, cgpa)
        editor.putString(KEY_BACKLOGS, backlogs)
        editor.putString(KEY_STAY_TYPE, stayType)
        editor.putString(KEY_ADDRESS, address)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getUserId(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_ID, null)
    }

    fun getUserType(context: Context): String? {
        return getPreferences(context).getString(KEY_USER_TYPE, null)
    }

    fun getName(context: Context): String? {
        return getPreferences(context).getString(KEY_NAME, null)
    }

    fun getEmail(context: Context): String? {
        return getPreferences(context).getString(KEY_EMAIL, null)
    }

    fun getDepartment(context: Context): String? {
        return getPreferences(context).getString(KEY_DEPARTMENT, null)
    }

    fun getYear(context: Context): String? {
        return getPreferences(context).getString(KEY_YEAR, null)
    }

    fun getStaffType(context: Context): String? {
        return getPreferences(context).getString(KEY_STAFF_TYPE, null)
    }

    fun getDesignation(context: Context): String? {
        return getPreferences(context).getString(KEY_DESIGNATION, null)
    }

    fun getBloodGroup(context: Context): String? {
        return getPreferences(context).getString(KEY_BLOOD_GROUP, null)
    }

    fun getPhone(context: Context): String? {
        return getPreferences(context).getString(KEY_PHONE, null)
    }

    fun getGender(context: Context): String? {
        return getPreferences(context).getString(KEY_GENDER, null)
    }

    fun getDob(context: Context): String? {
        return getPreferences(context).getString(KEY_DOB, null)
    }

    fun getCgpa(context: Context): String? {
        return getPreferences(context).getString(KEY_CGPA, null)
    }

    fun getBacklogs(context: Context): String? {
        return getPreferences(context).getString(KEY_BACKLOGS, null)
    }

    fun getStayType(context: Context): String? {
        return getPreferences(context).getString(KEY_STAY_TYPE, null)
    }

    fun getAddress(context: Context): String? {
        return getPreferences(context).getString(KEY_ADDRESS, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }

    // Helper function to get all user data for debugging
    fun getUserData(context: Context): Map<String, String?> {
        val prefs = getPreferences(context)
        return mapOf(
            "user_id" to prefs.getString(KEY_USER_ID, null),
            "user_type" to prefs.getString(KEY_USER_TYPE, null),
            "name" to prefs.getString(KEY_NAME, null),
            "email" to prefs.getString(KEY_EMAIL, null),
            "department" to prefs.getString(KEY_DEPARTMENT, null),
            "year" to prefs.getString(KEY_YEAR, null),
            "staff_type" to prefs.getString(KEY_STAFF_TYPE, null),
            "designation" to prefs.getString(KEY_DESIGNATION, null),
            "is_logged_in" to prefs.getBoolean(KEY_IS_LOGGED_IN, false).toString()
        )
    }
}