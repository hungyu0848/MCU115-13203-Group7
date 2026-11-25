package com.example.hw7

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * 倒數完成畫面 (FinishActivity)
 * 當番茄鐘倒數至 00:00 時顯示：
 * 1. 顯示 Focus Session Complete!
 * 2. 顯示 00:00
 * 3. 提供 Back to Home 按鈕返回主畫面
 */
class FinishActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val tvFinishTitle = findViewById<TextView>(R.id.tvFinishTitle)
        val tvFinishTimer = findViewById<TextView>(R.id.tvFinishTimer)
        val btnBackToHome = findViewById<Button>(R.id.btnBackToHome)

        tvFinishTitle.text = getString(R.string.title_finish)
        tvFinishTimer.text = getString(R.string.finish_timer)

        // 確保 Service 已完全結束
        val stopServiceIntent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        startService(stopServiceIntent)

        btnBackToHome.setOnClickListener {
            val mainIntent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(mainIntent)
            finish()
        }
    }
}
