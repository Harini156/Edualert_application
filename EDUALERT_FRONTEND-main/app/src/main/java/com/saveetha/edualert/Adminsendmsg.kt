package com.saveetha.edualert

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminSendMsg : Fragment() {

    private lateinit var tvRecipient: TextView
    private lateinit var etMessageTitle: EditText
    private lateinit var etMessageContent: EditText
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var tvAddAttachment: TextView
    private lateinit var layoutFileContainer: LinearLayout
    private lateinit var tvFileName: TextView
    private lateinit var ivRemoveFile: ImageView

    private var selectedFileUri: Uri? = null
    private var selectedOptions: ArrayList<String>? = null
    private var mainSendOption: String = "everyone"
    private var selectedRecipientInsideEveryone: String = "both"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adminsendmsg, container, false)

        // Initialize views
        tvRecipient = view.findViewById(R.id.tvRecipient)
        etMessageTitle = view.findViewById(R.id.etMessageTitle)
        etMessageContent = view.findViewById(R.id.etMessageContent)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        tvAddAttachment = view.findViewById(R.id.tvAddAttachment)
        layoutFileContainer = view.findViewById(R.id.layoutFileContainer)
        tvFileName = view.findViewById(R.id.tvFileName)
        ivRemoveFile = view.findViewById(R.id.ivRemoveFile)

        val etDepartment: EditText = view.findViewById(R.id.etDepartment)
        val spinnerStaffType: Spinner = view.findViewById(R.id.spinnerStaffType)
        val spinnerDesignation: Spinner = view.findViewById(R.id.spinnerDesignation)
        val spinnerYear: Spinner = view.findViewById(R.id.spinnerYear)
        val spinnerStayType: Spinner = view.findViewById(R.id.spinnerStayType)
        val spinnerGender: Spinner = view.findViewById(R.id.spinnerGender)
        val spinnerCgpa: Spinner = view.findViewById(R.id.spinnerCgpa)
        val spinnerBacklogs: Spinner = view.findViewById(R.id.spinnerBacklogs)

        setupSpinners(
            spinnerYear, spinnerStayType, spinnerGender,
            spinnerCgpa, spinnerBacklogs, spinnerStaffType, spinnerDesignation
        )

        // Get selected filters and main option
        selectedOptions = arguments?.getStringArrayList("selectedOptions")
        mainSendOption = arguments?.getString("mainSendOption") ?: "everyone"
        if (mainSendOption == "everyone") {
            selectedRecipientInsideEveryone =
                arguments?.getString("selectedRecipientInsideEveryone") ?: "both"
        }

        // Set "To:" text
        tvRecipient.text = when (mainSendOption) {
            "students" -> "To: Students"
            "staff" -> "To: Staff"
            "everyone" -> when (selectedRecipientInsideEveryone) {
                "student" -> "To: All Students"
                "staff" -> "To: All Staff"
                else -> "To: Everyone"
            }
            else -> "To: Everyone"
        }

        // Show only selected filters
        updateFilterVisibility(
            selectedOptions,
            spinnerYear, spinnerStayType, spinnerGender,
            spinnerCgpa, spinnerBacklogs, spinnerStaffType,
            spinnerDesignation, etDepartment
        )

        // Attachment picker
        tvAddAttachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Select Attachment"), 1001)
        }

        ivRemoveFile.setOnClickListener {
            selectedFileUri = null
            layoutFileContainer.visibility = View.GONE
            tvAddAttachment.text = " Add Attachment"
        }

        btnSendMessage.setOnClickListener {
            val title = etMessageTitle.text.toString().trim()
            val content = etMessageContent.text.toString().trim()

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter title and message", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            fun createPart(value: String?) =
                value?.takeIf { it.isNotEmpty() }?.let { RequestBody.create(MultipartBody.FORM, it) }

            val titlePart = createPart(title)!!
            val contentPart = createPart(content)!!
            val recipientTypePart = createPart(
                when (mainSendOption) {
                    "students" -> "student"
                    "staff" -> "staff"
                    "everyone" -> selectedRecipientInsideEveryone
                    else -> "both"
                }
            )!!

            // Optional filters (only if visible)
            val departmentPart =
                if (etDepartment.visibility == View.VISIBLE) createPart(etDepartment.text.toString()) else null
            val staffTypePart =
                if (spinnerStaffType.visibility == View.VISIBLE) createPart(spinnerStaffType.selectedItem.toString()) else null
            val designationPart =
                if (spinnerDesignation.visibility == View.VISIBLE) createPart(spinnerDesignation.selectedItem.toString()) else null
            val yearPart =
                if (spinnerYear.visibility == View.VISIBLE) createPart(spinnerYear.selectedItem.toString()) else null
            val stayTypePart =
                if (spinnerStayType.visibility == View.VISIBLE) createPart(spinnerStayType.selectedItem.toString()) else null
            val genderPart =
                if (spinnerGender.visibility == View.VISIBLE) createPart(spinnerGender.selectedItem.toString()) else null
            val cgpaPart =
                if (spinnerCgpa.visibility == View.VISIBLE) createPart(spinnerCgpa.selectedItem.toString()) else null
            val backlogsPart =
                if (spinnerBacklogs.visibility == View.VISIBLE) createPart(spinnerBacklogs.selectedItem.toString()) else null

            // Attachment
            var filePart: MultipartBody.Part? = null
            selectedFileUri?.let { uri ->
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()
                bytes?.let {
                    val mimeType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
                    val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), it)
                    val fileName = uri.lastPathSegment ?: "attachment"
                    filePart = MultipartBody.Part.createFormData("attachment", fileName, requestFile)
                }
            }
            Toast.makeText(requireContext(),"Please wait...", Toast.LENGTH_LONG).show()

            // API call
            ApiClient.instance.sendMessage(
                titlePart, contentPart, recipientTypePart,
                departmentPart, staffTypePart, designationPart,
                yearPart, stayTypePart, genderPart, cgpaPart, backlogsPart,
                filePart
            ).enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), response.body()?.message ?: "Message Sent!", Toast.LENGTH_LONG).show()
                        requireActivity().supportFragmentManager.popBackStack()
                    } else {
                        Log.d("API_ERROR", "Error: ${response.errorBody()?.string()}")
                        Toast.makeText(requireContext(), "Error sending message", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            tvAddAttachment.text = "Attachment Added"
            val fileName = selectedFileUri?.let { uri ->
                var result: String? = null
                val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (it.moveToFirst()) result = it.getString(nameIndex)
                }
                result ?: uri.lastPathSegment
            }
            if (!fileName.isNullOrEmpty()) {
                layoutFileContainer.visibility = View.VISIBLE
                tvFileName.text = "📎 $fileName"
            }
        }
    }

    private fun updateFilterVisibility(
        selectedOptions: ArrayList<String>?,
        spinnerYear: Spinner,
        spinnerStayType: Spinner,
        spinnerGender: Spinner,
        spinnerCgpa: Spinner,
        spinnerBacklogs: Spinner,
        spinnerStaffType: Spinner,
        spinnerDesignation: Spinner,
        etDepartment: EditText
    ) {
        if (selectedOptions == null) return

        // Hide all first
        spinnerYear.visibility = View.GONE
        spinnerStayType.visibility = View.GONE
        spinnerGender.visibility = View.GONE
        spinnerCgpa.visibility = View.GONE
        spinnerBacklogs.visibility = View.GONE
        spinnerStaffType.visibility = View.GONE
        spinnerDesignation.visibility = View.GONE
        etDepartment.visibility = View.GONE

        // Show only selected
        selectedOptions.forEach { option ->
            when (option) {
                "Year" -> spinnerYear.visibility = View.VISIBLE
                "Stay Type" -> spinnerStayType.visibility = View.VISIBLE
                "Gender" -> spinnerGender.visibility = View.VISIBLE
                "CGPA" -> spinnerCgpa.visibility = View.VISIBLE
                "Backlogs" -> spinnerBacklogs.visibility = View.VISIBLE
                "Staff Type" -> spinnerStaffType.visibility = View.VISIBLE
                "Designation" -> spinnerDesignation.visibility = View.VISIBLE
                "Department" -> etDepartment.visibility = View.VISIBLE
            }
        }
    }

    private fun setupSpinners(
        spinnerYear: Spinner,
        spinnerStayType: Spinner,
        spinnerGender: Spinner,
        spinnerCgpa: Spinner,
        spinnerBacklogs: Spinner,
        spinnerStaffType: Spinner,
        spinnerDesignation: Spinner
    ) {
        spinnerYear.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Year", "I Year", "II Year", "III Year", "IV Year"))

        spinnerStayType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Stay Type", "Hosteller", "Day Scholar"))

        spinnerGender.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Gender", "Male", "Female", "Other"))

        spinnerCgpa.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select CGPA Range", "Above 9.0", "8.0 - 8.9", "7.0 - 7.9", "Below 7.0"))

        spinnerBacklogs.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Backlogs", "0", "1-2", "3-5", "More than 5"))

        spinnerStaffType.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Staff Type", "Teaching", "Non-Teaching"))

        spinnerDesignation.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("Select Designation", "HOD", "Professor", "Assistant Professor", "Research Scholar"))
    }
}
