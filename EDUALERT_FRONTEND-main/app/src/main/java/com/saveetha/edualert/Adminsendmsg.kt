package com.saveetha.edualert

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import org.json.JSONObject

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
            Toast.makeText(requireContext(), "Attachment removed", Toast.LENGTH_SHORT).show()
        }
        
        // Make X button more prominent
        ivRemoveFile.setBackgroundColor(android.graphics.Color.parseColor("#FF6B6B"))
        ivRemoveFile.setPadding(8, 8, 8, 8)

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
                        val errorBody = response.errorBody()?.string() ?: "Unknown error"
                        val debugInfo = generateDebugInfo("API_RESPONSE_ERROR", "HTTP ${response.code()}", errorBody, null)
                        Log.e("API_ERROR", debugInfo)
                        showErrorWithDebug("Error sending message", debugInfo)
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    // Handle JSON parsing errors specifically
                    if (t is com.google.gson.JsonSyntaxException || t.message?.contains("malformed JSON") == true) {
                        handleMalformedJsonResponse(call, t)
                    } else {
                        val debugInfo = generateDebugInfo("NETWORK_FAILURE", t.javaClass.simpleName, t.message ?: "Unknown error", t)
                        Log.e("API_FAILURE", debugInfo)
                        showErrorWithDebug("Failed to send message", debugInfo)
                    }
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
                
                // Truncate filename if too long to ensure X button is always visible
                val displayName = if (fileName.length > 25) {
                    fileName.take(22) + "..."
                } else {
                    fileName
                }
                tvFileName.text = "📎 $displayName"
                
                // Force X button to be visible and clickable
                ivRemoveFile.visibility = View.VISIBLE
                ivRemoveFile.isClickable = true
                ivRemoveFile.isEnabled = true
                
                // Ensure X button stays on the right side
                ivRemoveFile.bringToFront()
                

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

    private fun generateDebugInfo(errorType: String, exceptionType: String, message: String, throwable: Throwable?): String {
        val timestamp = System.currentTimeMillis()
        val stackTrace = throwable?.stackTrace?.take(10)?.joinToString("\n") { it.toString() } ?: "No stack trace"
        
        return """
=== API FAILURE DEBUG ===
Timestamp: $timestamp
Exception Type: $exceptionType
Exception Message: $message
Cause: ${throwable?.cause?.message ?: "null"}
Stack Trace: $stackTrace
RESULT: $errorType
        """.trimIndent()
    }

    private fun showErrorWithDebug(userMessage: String, debugInfo: String) {
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Message Send Failed")
            .setMessage("$userMessage\n\nTap 'Copy Debug Info' to copy technical details to clipboard.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Copy Debug Info") { _, _ ->
                copyToClipboard(debugInfo)
                Toast.makeText(requireContext(), "Debug info copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .create()
        
        alertDialog.show()
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext().getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Debug Info", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun handleMalformedJsonResponse(call: Call<MessageResponse>, t: Throwable) {
        // Make a raw HTTP request to get the actual response
        val request = call.request()
        val client = okhttp3.OkHttpClient()
        
        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                requireActivity().runOnUiThread {
                    val debugInfo = generateDebugInfo("RAW_REQUEST_FAILURE", e.javaClass.simpleName, e.message ?: "Unknown error", e)
                    Log.e("RAW_REQUEST_ERROR", debugInfo)
                    showErrorWithDebug("Network request failed", debugInfo)
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                val rawResponse = response.body?.string() ?: ""
                requireActivity().runOnUiThread {
                    Log.d("RAW_RESPONSE", "Raw response: $rawResponse")
                    
                    // Try to extract JSON from the response
                    val jsonMatch = extractJsonFromResponse(rawResponse)
                    if (jsonMatch != null) {
                        try {
                            val jsonObject = org.json.JSONObject(jsonMatch)
                            val success = jsonObject.optBoolean("success", false)
                            val message = jsonObject.optString("message", "Unknown response")
                            
                            if (success) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                                requireActivity().supportFragmentManager.popBackStack()
                            } else {
                                Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            showRawResponseDialog(rawResponse)
                        }
                    } else {
                        showRawResponseDialog(rawResponse)
                    }
                }
            }
        })
    }

    private fun extractJsonFromResponse(response: String): String? {
        // Try to find JSON in the response
        val jsonStart = response.indexOf("{")
        val jsonEnd = response.lastIndexOf("}")
        
        return if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            response.substring(jsonStart, jsonEnd + 1)
        } else {
            null
        }
    }

    private fun showRawResponseDialog(rawResponse: String) {
        val alertDialog = android.app.AlertDialog.Builder(requireContext())
            .setTitle("Server Response")
            .setMessage("Raw server response:\n\n$rawResponse")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Copy Response") { _, _ ->
                copyToClipboard(rawResponse)
                Toast.makeText(requireContext(), "Response copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            .create()
        
        alertDialog.show()
    }
}
