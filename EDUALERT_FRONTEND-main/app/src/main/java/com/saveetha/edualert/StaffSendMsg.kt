package com.saveetha.edualert

import android.app.Activity
import android.app.ProgressDialog
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

class StaffSendMsg : Fragment() {

    private lateinit var tvRecipient: TextView
    private lateinit var tvFormTitle: TextView
    private lateinit var tvDepartment: TextView
    private lateinit var etMessageTitle: EditText
    private lateinit var etMessageContent: EditText
    private lateinit var btnSendMessage: MaterialButton
    private lateinit var tvAddAttachment: TextView
    private lateinit var layoutAddAttachment: LinearLayout
    private lateinit var layoutFileContainer: LinearLayout
    private lateinit var tvFileName: TextView
    private lateinit var ivRemoveFile: ImageView

    // Year checkboxes
    private lateinit var cbYear1: CheckBox
    private lateinit var cbYear2: CheckBox
    private lateinit var cbYear3: CheckBox
    private lateinit var cbYear4: CheckBox
    private lateinit var layoutYearSelection: LinearLayout

    // Designation checkboxes
    private lateinit var cbProfessor: CheckBox
    private lateinit var cbAsstProfessor: CheckBox
    private lateinit var cbResearch: CheckBox
    private lateinit var layoutDesignationSelection: LinearLayout

    private var selectedFileUri: Uri? = null
    private var recipientType: String? = null // "student" or "staff"
    private var senderId: String? = null // will fetch from SharedPreferences
    private var hodDept: String? = null // fetched department of HOD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recipientType = arguments?.getString("recipientType")

        // Fetch logged-in user ID from SharedPreferences and department
        val sharedPref = requireContext().getSharedPreferences("EduAlertPrefs", Context.MODE_PRIVATE)
        senderId = sharedPref.getString("USER_ID", null)
        hodDept = sharedPref.getString("DEPARTMENT", null) // ✅ corrected key
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_staff_send_msg, container, false)

        // Initialize views
        tvRecipient = view.findViewById(R.id.tvRecipient)
        tvFormTitle = view.findViewById(R.id.tvFormTitle)
        tvDepartment = view.findViewById(R.id.tvDepartment)
        etMessageTitle = view.findViewById(R.id.etMessageTitle)
        etMessageContent = view.findViewById(R.id.etMessageContent)
        btnSendMessage = view.findViewById(R.id.btnSendMessage)
        tvAddAttachment = view.findViewById(R.id.tvAddAttachment)
        layoutAddAttachment = view.findViewById(R.id.layoutAddAttachment)
        layoutFileContainer = view.findViewById(R.id.layoutFileContainer)
        tvFileName = view.findViewById(R.id.tvFileName)
        ivRemoveFile = view.findViewById(R.id.ivRemoveFile)

        // Year checkboxes
        cbYear1 = view.findViewById(R.id.cbYear1)
        cbYear2 = view.findViewById(R.id.cbYear2)
        cbYear3 = view.findViewById(R.id.cbYear3)
        cbYear4 = view.findViewById(R.id.cbYear4)
        layoutYearSelection = view.findViewById(R.id.layoutYearSelection)

        // Designation checkboxes
        cbProfessor = view.findViewById(R.id.cbProfessor)
        cbAsstProfessor = view.findViewById(R.id.cbAsstProfessor)
        cbResearch = view.findViewById(R.id.cbResearch)
        layoutDesignationSelection = view.findViewById(R.id.layoutDesignationSelection)

        // Show/hide fields based on recipient type
        if (recipientType == "student") {
            layoutYearSelection.visibility = View.VISIBLE
            layoutDesignationSelection.visibility = View.GONE
            tvRecipient.text = "To: Students"
            tvFormTitle.text = "Send Message to   Students"
        } else {
            layoutYearSelection.visibility = View.GONE
            layoutDesignationSelection.visibility = View.VISIBLE
            tvRecipient.text = "To: Staff"
            tvFormTitle.text = "Send Message to             Staff"
        }

        // Set department from HOD profile (SharedPreferences). If not found, show "-"
        if (!hodDept.isNullOrEmpty()) {
            tvDepartment.text = hodDept!!.uppercase()
        } else {
            tvDepartment.text = "-"
        }

        // Attachment picker
        layoutAddAttachment.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "*/*"
            startActivityForResult(Intent.createChooser(intent, "Select Attachment"), 1001)
        }

        // Remove file
        ivRemoveFile.setOnClickListener {
            selectedFileUri = null
            layoutFileContainer.visibility = View.GONE
            tvAddAttachment.text = " Add Attachment"
        }

        // Send message
        btnSendMessage.setOnClickListener { sendMessage() }

        return view
    }

    private fun sendMessage() {
        val title = etMessageTitle.text.toString().trim()
        val content = etMessageContent.text.toString().trim()
        val department = tvDepartment.text.toString().trim()

        if (title.isEmpty() || content.isEmpty() || department.isEmpty() || senderId.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        val yearSelected = mutableListOf<String>()
        if (recipientType == "student") {
            if (cbYear1.isChecked) yearSelected.add("1")
            if (cbYear2.isChecked) yearSelected.add("2")
            if (cbYear3.isChecked) yearSelected.add("3")
            if (cbYear4.isChecked) yearSelected.add("4")
        }

        val designationSelected = mutableListOf<String>()
        if (recipientType == "staff") {
            if (cbProfessor.isChecked) designationSelected.add("Professor")
            if (cbAsstProfessor.isChecked) designationSelected.add("Assistant Professor")
            if (cbResearch.isChecked) designationSelected.add("Research Scholar")
        }

        fun createPart(value: String?) =
            value?.takeIf { it.isNotEmpty() }?.let { RequestBody.create(MultipartBody.FORM, it) }

        val senderPart = createPart(senderId)
        val titlePart = createPart(title)!!
        val contentPart = createPart(content)!!
        val recipientTypePart = createPart(recipientType)!!
        val departmentPart = createPart(department)
        val yearPart = if (yearSelected.isNotEmpty()) createPart(yearSelected.joinToString(",")) else null
        val designationPart = if (designationSelected.isNotEmpty()) createPart(designationSelected.joinToString(",")) else null

        val filePart: MultipartBody.Part? = selectedFileUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            bytes?.let {
                val mimeType = requireContext().contentResolver.getType(uri) ?: "application/octet-stream"
                val requestFile = RequestBody.create(mimeType.toMediaTypeOrNull(), it)
                MultipartBody.Part.createFormData("attachment", uri.lastPathSegment ?: "attachment", requestFile)
            }
        }

        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Sending message...")
        progressDialog.setCancelable(false)
        progressDialog.show()
        btnSendMessage.isEnabled = false

        ApiClient.instance.sendStaffMessage(
            senderPart!!, titlePart, contentPart, recipientTypePart,
            departmentPart, yearPart, designationPart, filePart
        ).enqueue(object : Callback<HodMessageResponse> {
            override fun onResponse(call: Call<HodMessageResponse>, response: Response<HodMessageResponse>) {
                progressDialog.dismiss()
                btnSendMessage.isEnabled = true

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

            override fun onFailure(call: Call<HodMessageResponse>, t: Throwable) {
                progressDialog.dismiss()
                btnSendMessage.isEnabled = true
                
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
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Debug Info", text)
        clipboard.setPrimaryClip(clip)
    }

    private fun handleMalformedJsonResponse(call: Call<HodMessageResponse>, t: Throwable) {
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
