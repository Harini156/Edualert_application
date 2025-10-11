package com.saveetha.edualert

import android.app.Activity
import android.app.ProgressDialog
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
                    Log.e("API_ERROR", "Error: ${response.errorBody()?.string()}")
                    Toast.makeText(requireContext(), "Error sending message", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<HodMessageResponse>, t: Throwable) {
                progressDialog.dismiss()
                btnSendMessage.isEnabled = true
                Log.e("API_ERROR", "Error: ${t.message}")
                Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_LONG).show()
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
}
