package com.saveetha.edualert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class Admineveryone : Fragment() {

    private lateinit var btnSelectContinue: MaterialButton
    private lateinit var cardStudents: MaterialCardView
    private lateinit var cardStaff: MaterialCardView
    private lateinit var cardBoth: MaterialCardView

    private var selectedRecipient: String? = null  // "student", "staff", or "both"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admineveryone, container, false)

        btnSelectContinue = view.findViewById(R.id.btnSelectContinue)
        cardStudents = view.findViewById(R.id.cardStudents)
        cardStaff = view.findViewById(R.id.cardStaff)
        cardBoth = view.findViewById(R.id.cardBoth)

        cardStudents.setOnClickListener {
            cardStudents.isChecked = true
            cardStaff.isChecked = false
            cardBoth.isChecked = false
            selectedRecipient = "student"
        }

        cardStaff.setOnClickListener {
            cardStudents.isChecked = false
            cardStaff.isChecked = true
            cardBoth.isChecked = false
            selectedRecipient = "staff"
        }

        cardBoth.setOnClickListener {
            cardStudents.isChecked = false
            cardStaff.isChecked = false
            cardBoth.isChecked = true
            selectedRecipient = "both"
        }

        btnSelectContinue.setOnClickListener {
            if (selectedRecipient == null) {
                Toast.makeText(requireContext(), "Please select a recipient", Toast.LENGTH_SHORT).show()
            } else {
                val fragment = AdminSendMsg()
                val bundle = Bundle()
                bundle.putString("mainSendOption", "everyone")
                bundle.putString("selectedRecipientInsideEveryone", selectedRecipient)
                fragment.arguments = bundle

                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentAdminContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }
}
