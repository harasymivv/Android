package com.example.lab3

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

class ResultFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        val resultTextView = view.findViewById<TextView>(R.id.result_text_view)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        val question = arguments?.getString("question", "")
        val answer = arguments?.getString("answer", "")

        resultTextView.text = "Питання: $question\nВідповідь: $answer"

        cancelButton.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
            (activity?.supportFragmentManager?.findFragmentById(R.id.fragment_container) as? InputFragment)?.clearForm()
        }

        return view
    }
}