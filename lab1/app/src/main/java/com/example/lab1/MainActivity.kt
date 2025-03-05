package com.example.lab1

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val submitButton = findViewById<Button>(R.id.submitButton)
        val questionEditText = findViewById<EditText>(R.id.questionEditText)
        val answerRadioGroup = findViewById<RadioGroup>(R.id.answerRadioGroup)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        submitButton.setOnClickListener { v: View? ->
            val question = questionEditText.text.toString()
            val selectedRadioButtonId = answerRadioGroup.checkedRadioButtonId

            if (question.isEmpty() || selectedRadioButtonId == -1) {
                showIncompleteDataDialog()
                return@setOnClickListener
            }

            val answer = if ((selectedRadioButtonId == R.id.yesRadioButton)) "Yes" else "No"

            val result = "Question: $question\nAnswer: $answer"
            resultTextView.text = result
        }
    }

    private fun showIncompleteDataDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
            .setMessage("Please fill in all fields.")
            .setPositiveButton("ОК", null)
            .show()
    }
}