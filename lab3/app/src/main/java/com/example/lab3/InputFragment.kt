package com.example.lab3

import android.content.Context
import android.content.Intent
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
    private lateinit var openButton: Button // Нова кнопка

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
        openButton = view.findViewById(R.id.open_button) // Ініціалізація нової кнопки

        okButton.setOnClickListener {
            val question = questionEditText.text.toString()
            if (question.isEmpty()) {
                Toast.makeText(context, "Будь ласка, введіть питання", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButtonId = radioGroup.checkedRadioButtonId
            if (selectedRadioButtonId == -1) {
                Toast.makeText(context, "Будь ласка, оберіть відповідь", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRadioButton = view.findViewById<RadioButton>(selectedRadioButtonId)
            val answer = selectedRadioButton.text.toString()

            inputListener.onInputSent(question, answer)
        }


        openButton.setOnClickListener {
            val intent = Intent(context, FileViewActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    fun clearForm() {
        questionEditText.text.clear()
        radioGroup.clearCheck()
    }
}