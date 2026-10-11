package com.example.hw3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SecActivity : AppCompatActivity() {

    private lateinit var tvReceivedFromMain: TextView
    private lateinit var etSecText: EditText
    private lateinit var btnReturnToMain: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sec)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvReceivedFromMain = findViewById(R.id.tvReceivedFromMain)
        etSecText = findViewById(R.id.etSecText)
        btnReturnToMain = findViewById(R.id.btnReturnToMain)

        val bundle = intent.extras
        val receivedText = bundle?.getString("main_text") ?: ""
        tvReceivedFromMain.text = "第一頁傳來的文字：$receivedText"

        btnReturnToMain.setOnClickListener {
            val returnIntent = Intent()
            val returnBundle = Bundle()
            returnBundle.putString("sec_text", etSecText.text.toString())
            returnIntent.putExtras(returnBundle)
            setResult(RESULT_OK, returnIntent)
            finish()
        }
    }
}