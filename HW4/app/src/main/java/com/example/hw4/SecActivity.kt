package com.example.hw4

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SecActivity : AppCompatActivity() {

    private lateinit var edDrink: EditText
    private lateinit var rgSugar: RadioGroup
    private lateinit var rgIce: RadioGroup
    private lateinit var btnSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sec)

        edDrink = findViewById(R.id.edDrink)
        rgSugar = findViewById(R.id.rgSugar)
        rgIce = findViewById(R.id.rgIce)
        btnSend = findViewById(R.id.btnSend)

        btnSend.setOnClickListener {
            val drinkText = edDrink.text.toString()
            if (drinkText.isEmpty()) {
                Toast.makeText(this, "請輸入飲料名稱", Toast.LENGTH_SHORT).show()
            } else {
                val selectedSugarId = rgSugar.checkedRadioButtonId
                val sugarText = findViewById<RadioButton>(selectedSugarId)?.text?.toString() ?: "全糖"

                val selectedIceId = rgIce.checkedRadioButtonId
                val iceText = findViewById<RadioButton>(selectedIceId)?.text?.toString() ?: "正常冰"

                val b = Bundle()
                b.putString("drink", drinkText)
                b.putString("sugar", sugarText)
                b.putString("ice", iceText)

                val intent = Intent().putExtras(b)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }
    }
}
