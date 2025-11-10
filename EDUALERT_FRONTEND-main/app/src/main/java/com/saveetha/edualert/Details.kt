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
            "student" -> {
                setupStudentFields()
                if (isEditMode) {
                    fetchAndPopulateStudentData(userId)
                }
            }
            "staff" -> {
                setupStaffFields()
                if (isEditMode) {
                    fetchAndPopulateStaffData(userId)
                }
            }
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

        // Set DOB format guidance for new student registrations
        val dobField = findViewById<EditText>(R.id.dobField)
        dobField.hint = "Enter Date of Birth (YYYY-MM-DD) e.g., 2000-12-25"

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
        
        // Set DOB format guidance for new staff registrations
        val dobFieldStaff = findViewById<EditText>(R.id.dobFieldStaff)
        dobFieldStaff.hint = "Enter Date of Birth (YYYY-MM-DD) e.g., 1985-06-15"
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

    // -------------------------
    // Fetch and Populate Student Data for Edit Mode
    // -------------------------
    private fun fetchAndPopulateStudentData(userId: String) {
        ApiClient.instance.getStudentProfile(userId)
            .enqueue(object : Callback<StudentDetailsResponse> {
                override fun onResponse(call: Call<StudentDetailsResponse>, response: Response<StudentDetailsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        if (responseBody.status == "success" && responseBody.student != null) {
                            val student = responseBody.student!!
                            
                            // Populate all fields with existing data
                            val dobValue = student.dob ?: ""
                            val dobField = findViewById<EditText>(R.id.dobField)
                            
                            // Always set helpful hint for DOB format
                            dobField.hint = "Enter Date of Birth (YYYY-MM-DD) e.g., 2000-12-25"
                            
                            if (dobValue.isEmpty()) {
                                dobField.setText("")
                                Toast.makeText(this@Details, "Please enter your Date of Birth in format: YYYY-MM-DD", Toast.LENGTH_LONG).show()
                            } else {
                                dobField.setText(dobValue)
                            }
                            findViewById<EditText>(R.id.bloodGroupField).setText(student.blood_group ?: "")
                            findViewById<EditText>(R.id.departmentFieldStudent).setText(student.department ?: "")
                            cgpaField.setText(student.cgpa ?: "")
                            backlogsField.setText(student.backlogs ?: "")
                            findViewById<EditText>(R.id.phoneField).setText(student.phone ?: "")
                            findViewById<EditText>(R.id.addressField).setText(student.address ?: "")

                            // Set spinner selections
                            setSpinnerSelection(genderSpinner, student.gender ?: "")
                            setYearSpinnerSelection(yearSpinner, student.year ?: "")
                            setSpinnerSelection(stayTypeSpinner, student.stay_type ?: "")
                            
                            Toast.makeText(this@Details, "Profile data loaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@Details, "No student data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Details, "Failed to load student data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<StudentDetailsResponse>, t: Throwable) {
                    Toast.makeText(this@Details, "Error loading data: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // -------------------------
    // Fetch and Populate Staff Data for Edit Mode
    // -------------------------
    private fun fetchAndPopulateStaffData(userId: String) {
        ApiClient.instance.getStaffProfile(userId)
            .enqueue(object : Callback<StaffDetailsResponse> {
                override fun onResponse(call: Call<StaffDetailsResponse>, response: Response<StaffDetailsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val responseBody = response.body()!!
                        if (responseBody.status == "success" && responseBody.staff != null) {
                            val staff = responseBody.staff!!
                            
                            // Populate staff fields with DOB format hint
                            val dobFieldStaff = findViewById<EditText>(R.id.dobFieldStaff)
                            dobFieldStaff.setText(staff.dob ?: "")
                            dobFieldStaff.hint = "Enter Date of Birth (YYYY-MM-DD) e.g., 1985-06-15"
                            findViewById<EditText>(R.id.phoneField).setText(staff.phone ?: "")
                            findViewById<EditText>(R.id.addressField).setText(staff.address ?: "")

                            // Set staff type spinner
                            setSpinnerSelection(staffTypeSpinner, staff.staff_type ?: "")

                            // If teaching staff, populate department and designation
                            if (staff.staff_type?.equals("Teaching", true) == true) {
                                departmentStaffField.setText(staff.department ?: "")
                                setSpinnerSelection(designationSpinner, staff.designation ?: "")
                                staffDeptDesignationLayout.visibility = LinearLayout.VISIBLE
                            }
                            
                            Toast.makeText(this@Details, "Staff data loaded", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@Details, "No staff data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Details, "Failed to load staff data", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<StaffDetailsResponse>, t: Throwable) {
                    Toast.makeText(this@Details, "Error loading data: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // -------------------------
    // Helper method to set spinner selection
    // -------------------------
    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString().equals(value, ignoreCase = true)) {
                spinner.setSelection(i)
                break
            }
        }
    }
    
    // -------------------------
    // Helper method to set year spinner with number to roman conversion
    // -------------------------
    private fun setYearSpinnerSelection(yearSpinner: Spinner, yearValue: String?) {
        if (yearValue.isNullOrEmpty()) return
        
        val yearMapping = mapOf(
            "1" to "I Year",
            "2" to "II Year", 
            "3" to "III Year",
            "4" to "IV Year"
        )
        
        val displayValue = yearMapping[yearValue] ?: yearValue
        setSpinnerSelection(yearSpinner, displayValue)
    }

}
