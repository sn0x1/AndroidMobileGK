package com.example.variant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val surnameEdit = findViewById<EditText>(R.id.surnameEdit)
        val nameEdit = findViewById<EditText>(R.id.nameEdit)
        val groupEdit = findViewById<EditText>(R.id.groupEdit)

        val calcButton = findViewById<Button>(R.id.calcButton)
        val doneButton = findViewById<Button>(R.id.doneButton)
        val resultText = findViewById<TextView>(R.id.resultText)

        calcButton.setOnClickListener {
            val surname = surnameEdit.text.toString()
            val name = nameEdit.text.toString()
            val group = groupEdit.text.toString()

            val sumCodes = (surname + name + group).sumOf { it.code }
            val variant = (sumCodes % 10) + 1

            resultText.text =
                "$surname\n$name\n$group\n$variant"
        }

        doneButton.setOnClickListener {
            finish()
        }
    }
}