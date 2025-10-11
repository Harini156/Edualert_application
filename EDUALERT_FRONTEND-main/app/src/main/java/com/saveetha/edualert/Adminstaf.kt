package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class AdminStaf : Fragment() {

    private lateinit var btnStaffContinue: MaterialButton
    private lateinit var cardStaffDepartment: MaterialCardView
    private lateinit var cardStaffType: MaterialCardView
    private lateinit var cardDesignation: MaterialCardView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adminstaf, container, false)

        btnStaffContinue = view.findViewById(R.id.btnStaffContinue)
        cardStaffDepartment = view.findViewById(R.id.cardStaffDepartment)
        cardStaffType = view.findViewById(R.id.cardStaffType)
        cardDesignation = view.findViewById(R.id.cardDesignation)

        // Toggle check state
        cardStaffDepartment.setOnClickListener { cardStaffDepartment.isChecked = !cardStaffDepartment.isChecked }
        cardStaffType.setOnClickListener { cardStaffType.isChecked = !cardStaffType.isChecked }
        cardDesignation.setOnClickListener { cardDesignation.isChecked = !cardDesignation.isChecked }

        btnStaffContinue.setOnClickListener {
            val selectedOptions = mutableListOf<String>()
            if (cardStaffDepartment.isChecked) selectedOptions.add("Department")
            if (cardStaffType.isChecked) selectedOptions.add("Staff Type")
            if (cardDesignation.isChecked) selectedOptions.add("Designation")


            if (selectedOptions.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one option", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val fragment = AdminSendMsg()
            val bundle = Bundle()
            bundle.putStringArrayList("selectedOptions", ArrayList(selectedOptions))
            bundle.putString("mainSendOption", "staff")
            fragment.arguments = bundle

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentAdminContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}
