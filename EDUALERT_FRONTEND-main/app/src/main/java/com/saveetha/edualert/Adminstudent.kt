package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AdminStudent : Fragment() {

    private lateinit var btnStudentContinue: MaterialButton
    private lateinit var cardDepartment: MaterialCardView
    private lateinit var cardYear: MaterialCardView
    private lateinit var cardStayType: MaterialCardView
    private lateinit var cardGender: MaterialCardView
    private lateinit var cardCgpa: MaterialCardView
    private lateinit var cardBacklogs: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adminstudent, container, false)

        btnStudentContinue = view.findViewById(R.id.btnContinue)
        cardDepartment = view.findViewById(R.id.cardDepartment)
        cardYear = view.findViewById(R.id.cardYear)
        cardStayType = view.findViewById(R.id.cardStayType)
        cardGender = view.findViewById(R.id.cardGender)
        cardCgpa = view.findViewById(R.id.cardCGPA)
        cardBacklogs = view.findViewById(R.id.cardBacklogs)

        // Toggle check state
        cardDepartment.setOnClickListener { cardDepartment.isChecked = !cardDepartment.isChecked }
        cardYear.setOnClickListener { cardYear.isChecked = !cardYear.isChecked }
        cardStayType.setOnClickListener { cardStayType.isChecked = !cardStayType.isChecked }
        cardGender.setOnClickListener { cardGender.isChecked = !cardGender.isChecked }
        cardCgpa.setOnClickListener { cardCgpa.isChecked = !cardCgpa.isChecked }
        cardBacklogs.setOnClickListener { cardBacklogs.isChecked = !cardBacklogs.isChecked }

        btnStudentContinue.setOnClickListener {
            val selectedOptions = mutableListOf<String>()
            if (cardDepartment.isChecked) selectedOptions.add("Department")
            if (cardYear.isChecked) selectedOptions.add("Year")
            if (cardStayType.isChecked) selectedOptions.add("Stay Type")
            if (cardGender.isChecked) selectedOptions.add("Gender")
            if (cardCgpa.isChecked) selectedOptions.add("CGPA")
            if (cardBacklogs.isChecked) selectedOptions.add("Backlogs")

            if (selectedOptions.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fragment = AdminSendMsg()
            val bundle = Bundle()
            bundle.putStringArrayList("selectedOptions", ArrayList(selectedOptions))
            bundle.putString("mainSendOption", "students")
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
