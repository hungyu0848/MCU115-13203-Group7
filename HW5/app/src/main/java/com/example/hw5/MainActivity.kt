package com.example.hw5

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnToast = findViewById<Button>(R.id.btnToast)
        val btnSnackBar = findViewById<Button>(R.id.btnSnackBar)
        val btnDialog1 = findViewById<Button>(R.id.btnDialog1)
        val btnDialog2 = findViewById<Button>(R.id.btnDialog2)
        val btnDialog3 = findViewById<Button>(R.id.btnDialog3)

        btnToast.setOnClickListener {
            showToast("預設 Toast")
        }

        btnSnackBar.setOnClickListener { view ->
            Snackbar.make(view, "按鈕式 Snackbar", Snackbar.LENGTH_INDEFINITE)
                .setAction("關閉") { }
                .show()
        }

        btnDialog1.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("按鈕式 AlertDialog")
                .setMessage("AlertDialog 內容")
                .setNeutralButton("左按鈕") { _, _ ->
                    showToast("左按鈕")
                }
                .setNegativeButton("中按鈕") { _, _ ->
                    showToast("中按鈕")
                }
                .setPositiveButton("右按鈕") { _, _ ->
                    showToast("右按鈕")
                }
                .show()
        }

        btnDialog2.setOnClickListener {
            val item = arrayOf(
                "選項 1",
                "選項 2",
                "選項 3",
                "選項 4",
                "選項 5"
            )
            AlertDialog.Builder(this)
                .setTitle("列表式 AlertDialog")
                .setItems(item) { _, which ->
                    showToast("你選的是：${item[which]}")
                }
                .show()
        }

        btnDialog3.setOnClickListener {
            val item = arrayOf(
                "選項 1",
                "選項 2",
                "選項 3",
                "選項 4",
                "選項 5"
            )
            var position = -1
            AlertDialog.Builder(this)
                .setTitle("單選式 AlertDialog")
                .setSingleChoiceItems(item, -1) { _, which ->
                    position = which
                }
                .setPositiveButton("確定") { _, _ ->
                    if (position != -1) {
                        showToast("你選的是：${item[position]}")
                    } else {
                        showToast("尚未選擇項目")
                    }
                }
                .show()
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
