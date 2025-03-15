package com.example.lab2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast

class InputFragment : Fragment() {

    private lateinit var inputListener: OnInputListener
    private lateinit var questionEditText: EditText
    private lateinit var radioGroup: RadioGroup
    private lateinit var okButton: Button

    interface OnInputListener {
        fun onInputSent(question: String, answer: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnInputListener) {
            inputListener = context
        } else {
            throw RuntimeException("$context must implement OnInputListener")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_input, container, false)

        questionEditText = view.findViewById(R.id.question_edit_text)
        radioGroup = view.findViewById(R.id.radio_group)
        okButton = view.findViewById(R.id.ok_button)

        okButton.setOnClickListener {
            val question = questionEditText.text.toString()
            if (question.isEmpty()) {
                Toast.makeText(context, "Please enter a question", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId == -1) {
                Toast.makeText(context, "Please select an answer", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = view.findViewById<RadioButton>(selectedRadioButtonId)
            val answer = selectedRadioButton.text.toString()

            inputListener.onInputSent(question, answer)
        }

        return view
    }

    fun clearForm() {
        questionEditText.text.clear()
        radioGroup.clearCheck()
    }
}