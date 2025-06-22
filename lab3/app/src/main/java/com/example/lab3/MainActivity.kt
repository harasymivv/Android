package com.example.lab3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), InputFragment.OnInputListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, InputFragment())
                .commit()
        }
    }

    override fun onInputSent(question: String, answer: String) {

        saveToFile(question, answer)

        val resultFragment = ResultFragment().apply {
            arguments = Bundle().apply {
                putString("question", question)
                putString("answer", answer)
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, resultFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun saveToFile(question: String, answer: String) {
        try {
            val fileName = "questions_answers.txt"
            val file = File(filesDir, fileName)

            val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val dataToSave = "[$timestamp] Question: $question | Answer: $answer\n"


            val fileOutputStream = FileOutputStream(file, true)
            fileOutputStream.write(dataToSave.toByteArray())
            fileOutputStream.close()

            Toast.makeText(this, "Дані успішно збережено!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка збереження: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}