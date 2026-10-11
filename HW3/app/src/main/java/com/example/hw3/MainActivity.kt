package com.example.hw3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var etMainText: EditText
    private lateinit var btnSendToSec: Button
    private lateinit var tvResultFromSec: TextView

    private val secActivityLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val returnedText = data?.getStringExtra("sec_text")
                    ?: data?.extras?.getString("sec_text")
                    ?: ""
                tvResultFromSec.text = "第二頁回傳的文字：$returnedText"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        etMainText = findViewById(R.id.etMainText)
        btnSendToSec = findViewById(R.id.btnSendToSec)
        tvResultFromSec = findViewById(R.id.tvResultFromSec)

        btnSendToSec.setOnClickListener {
            val intent = Intent(this, SecActivity::class.java)
            val bundle = Bundle()
            bundle.putString("main_text", etMainText.text.toString())
            intent.putExtras(bundle)
            secActivityLauncher.launch(intent)
        }
    }
}