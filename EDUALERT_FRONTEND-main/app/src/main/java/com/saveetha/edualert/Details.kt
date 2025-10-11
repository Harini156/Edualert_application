package com.saveetha.edualert

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Details : AppCompatActivity() {

    private lateinit var studentFields: LinearLayout
    private lateinit var staffFields: LinearLayout
    private lateinit var continueButton: Button
    private lateinit var staffDeptDesignationLayout: LinearLayout
    private lateinit var staffTypeSpinner: Spinner
    private lateinit var departmentStaffField: EditText
    private lateinit var designationSpinner: Spinner

    // Student Spinners (still using for other fields)
    private lateinit var genderSpinner: Spinner
    private lateinit var yearSpinner: Spinner
    private lateinit var stayTypeSpinner: Spinner

    // Student EditTexts for CGPA and Backlogs
    private lateinit var cgpaField: EditText
    private lateinit var backlogsField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        // Initialize Views
        studentFields = findViewById(R.id.studentExtraFields)
        staffFields = findViewById(R.id.staffExtraFields)
        continueButton = findViewById(R.id.continueButton)
        staffDeptDesignationLayout = findViewById(R.id.staffDeptDesignationLayout)
        staffTypeSpinner = findViewById(R.id.staffTypeSpinner)
        departmentStaffField = findViewById(R.id.departmentFieldStaff)
        designationSpinner = findViewById(R.id.designationSpinner)

        // Student Views
        genderSpinner = findViewById(R.id.genderSpinner)
        yearSpinner = findViewById(R.id.yearSpinner)
        stayTypeSpinner = findViewById(R.id.stayTypeSpinner)
        cgpaField = findViewById(R.id.cgpaField)        // updated
        backlogsField = findViewById(R.id.backlogsField) // updated

        val userType = intent.getStringExtra("userType")?.lowercase()
        val userId = intent.getStringExtra("userId") ?: ""
        val isEditMode = intent.getBooleanExtra("isEditMode", false)

        continueButton.text = if (isEditMode) "Save" else "Continue"

        when (userType) {
            "student" -> setupStudentFields()
            "staff" -> setupStaffFields()
            else -> {
                startActivity(Intent(this, Login::class.java))
                finish()
            }
        }

        // -------------------------
        // Staff Type Spinner Setup
        // -------------------------
        val staffTypeList = listOf("Select Staff Type", "Teaching", "Non-Teaching")
        staffTypeSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, staffTypeList)

        val designationList = listOf("Select Designation", "HOD", "Professor", "Assistant Professor", "Research Scholar")
        designationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, designationList)

        staffDeptDesignationLayout.visibility = LinearLayout.GONE

        staffTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedType = staffTypeSpinner.selectedItem.toString()
                if (selectedType.equals("Teaching", true)) {
                    staffDeptDesignationLayout.visibility = LinearLayout.VISIBLE
                } else {
                    staffDeptDesignationLayout.visibility = LinearLayout.GONE
                    departmentStaffField.text.clear()
                    designationSpinner.setSelection(0)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        continueButton.setOnClickListener {
            if (userType == "student") saveStudentDetails(userId, isEditMode)
            else if (userType == "staff") saveStaffDetails(userId, isEditMode)
        }
    }

    // -------------------------
    // Setup Student Fields
    // -------------------------
    private fun setupStudentFields() {
        studentFields.visibility = LinearLayout.VISIBLE
        staffFields.visibility = LinearLayout.GONE

        genderSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Gender", "Male", "Female", "Other")
        )
        yearSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Year", "I Year", "II Year", "III Year", "IV Year")
        )
        stayTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Stay Type", "Hosteller", "Day Scholar")
        )
    }

    // -------------------------
    // Setup Staff Fields
    // -------------------------
    private fun setupStaffFields() {
        studentFields.visibility = LinearLayout.GONE
        staffFields.visibility = LinearLayout.VISIBLE
    }

    // -------------------------
    // Save Student Details
    // -------------------------
    private fun saveStudentDetails(userId: String, isEditMode: Boolean) {
        val dob = findViewById<EditText>(R.id.dobField).text.toString().trim()
        val gender = genderSpinner.selectedItem.toString()
        val bloodGroup = findViewById<EditText>(R.id.bloodGroupField).text.toString().trim()
        val department = findViewById<EditText>(R.id.departmentFieldStudent).text.toString().trim()
        val year = yearSpinner.selectedItem.toString()
        val cgpa = cgpaField.text.toString().trim()       // updated
        val backlogs = backlogsField.text.toString().trim() // updated
        val stayType = stayTypeSpinner.selectedItem.toString()
        val phone = findViewById<EditText>(R.id.phoneField).text.toString().trim()
        val address = findViewById<EditText>(R.id.addressField).text.toString().trim()

        // Validate all fields
        if (dob.isEmpty() || gender == "Select Gender" || bloodGroup.isEmpty() || department.isEmpty() ||
            year == "Select Year" || cgpa.isEmpty() || backlogs.isEmpty() || stayType == "Select Stay Type" ||
            phone.isEmpty() || address.isEmpty()
        ) {
            Toast.makeText(this, "Please fill all student fields", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.instance.saveStudentDetails(
            userId, dob, gender, bloodGroup, department, year, cgpa,
            backlogs, stayType, phone, address
        ).enqueue(object : Callback<GenericResponse> {
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    Toast.makeText(this@Details, "Student details saved", Toast.LENGTH_SHORT).show()
                    if (isEditMode) finish() else {
                        startActivity(Intent(this@Details, Login::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(this@Details, "Error: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                Toast.makeText(this@Details, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // -------------------------
    // Save Staff Details
    // -------------------------
    private fun saveStaffDetails(userId: String, isEditMode: Boolean) {
        val dob = findViewById<EditText>(R.id.dobFieldStaff).text.toString().trim()
        val staffType = staffTypeSpinner.selectedItem.toString()
        val phone = findViewById<EditText>(R.id.phoneField).text.toString().trim()
        val address = findViewById<EditText>(R.id.addressField).text.toString().trim()

        var department: String? = null
        var designation: String? = null

        if (dob.isEmpty()) { Toast.makeText(this, "DOB is required", Toast.LENGTH_SHORT).show(); return }
        if (staffType == "Select Staff Type") { Toast.makeText(this, "Select staff type", Toast.LENGTH_SHORT).show(); return }
        if (phone.isEmpty()) { Toast.makeText(this, "Phone is required", Toast.LENGTH_SHORT).show(); return }
        if (address.isEmpty()) { Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show(); return }

        if (staffType.equals("Teaching", true)) {
            department = departmentStaffField.text.toString().trim()
            designation = designationSpinner.selectedItem.toString().trim()
            if (department.isEmpty()) { Toast.makeText(this, "Department is required for Teaching staff", Toast.LENGTH_SHORT).show(); return }
            if (designation.isEmpty() || designation == "Select Designation") { Toast.makeText(this, "Designation is required for Teaching staff", Toast.LENGTH_SHORT).show(); return }
        }

        ApiClient.instance.saveStaffDetails(userId, dob, staffType, department, designation, phone, address)
            .enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                    if (response.isSuccessful && response.body()?.status == "success") {
                        Toast.makeText(this@Details,
                            if (isEditMode) "Staff details updated" else "Staff details saved",
                            Toast.LENGTH_SHORT).show()
                        if (isEditMode) finish() else {
                            startActivity(Intent(this@Details, Login::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this@Details, "Error: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    Toast.makeText(this@Details, "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
