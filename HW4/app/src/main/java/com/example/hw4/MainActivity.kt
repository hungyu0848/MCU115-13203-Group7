package com.example.hw4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvMeal: TextView
    private lateinit var btnChoice: Button

    private val startActivityForResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent = result.data
                val drink = intent?.getStringExtra("drink") ?: ""
                val sugar = intent?.getStringExtra("sugar") ?: ""
                val ice = intent?.getStringExtra("ice") ?: ""
                tvMeal.text = "飲料：$drink\n\n甜度：$sugar\n\n冰塊：$ice"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvMeal = findViewById(R.id.tvMeal)
        btnChoice = findViewById(R.id.btnChoice)

        btnChoice.setOnClickListener {
            val intent = Intent(this, SecActivity::class.java)
            startActivityForResult.launch(intent)
        }
    }
}
