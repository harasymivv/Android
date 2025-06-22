package com.example.lab3

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FileViewActivity : AppCompatActivity() {

    private lateinit var tvFileContent: TextView
    private lateinit var btnBack: Button
    private lateinit var btnClear: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_file_view)

        tvFileContent = findViewById(R.id.tvFileContent)
        btnBack = findViewById(R.id.btnBack)
        btnClear = findViewById(R.id.btnClear)

        loadFileContent()

        btnBack.setOnClickListener {
            finish()
        }

        btnClear.setOnClickListener {
            clearFileContent()
        }
    }

    private fun loadFileContent() {
        try {
            val fileName = "questions_answers.txt"
            val file = File(filesDir, fileName)

            if (file.exists()) {
                val content = file.readText()
                if (content.isNotEmpty()) {
                    tvFileContent.text = content
                } else {
                    tvFileContent.text = "Файл існує, але порожній"
                }
            } else {
                tvFileContent.text = "Немає збережених даних"
            }
        } catch (e: Exception) {
            tvFileContent.text = "Помилка читання файлу: ${e.message}"
        }
    }

    private fun clearFileContent() {
        try {
            val fileName = "questions_answers.txt"
            val file = File(filesDir, fileName)

            if (file.exists()) {
                file.delete()
                tvFileContent.text = "Файл очищено"
                Toast.makeText(this, "Дані успішно видалено!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Немає даних для видалення", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Помилка видалення: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}