package com.example.hw7

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

/**
 * 智慧番茄鐘主畫面 (MainActivity)
 * 功能重點：
 * 1. 25:00 / MM:SS 倒數時間顯示。
 * 2. 狀態顯示 (Ready to Focus / Focusing...)。
 * 3. Start Focus 按鈕與 Stop 按鈕。
 * 4. 動態註冊 TimerBroadcastReceiver，即時接收並更新時間。
 * 5. 切換或重新進入 App 時（onResume）自動查詢並同步最新剩餘時間。
 * 6. 不會因旋轉螢幕或重複按 Start 產生多個計時器或 Thread。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvTimer: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var switchDebugMode: SwitchMaterial

    private var isReceiverRegistered = false
    private var pendingStartDuration: Int? = null

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                pendingStartDuration?.let { duration ->
                    startTimerService(duration)
                }
            } else {
                Toast.makeText(this, "請允許通知權限以使用前景計時服務", Toast.LENGTH_LONG).show()
                // 即使沒有通知權限，服務仍嘗試啟動
                pendingStartDuration?.let { duration ->
                    startTimerService(duration)
                }
            }
            pendingStartDuration = null
        }

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                TimerService.BROADCAST_TICK -> {
                    val remainingSeconds = intent.getIntExtra(TimerService.EXTRA_REMAINING_SECONDS, 1500)
                    updateTimerUI(remainingSeconds, isRunning = true)
                }
                TimerService.BROADCAST_FINISH -> {
                    updateTimerUI(0, isRunning = false)
                    navigateToFinishScreen()
                }
                TimerService.BROADCAST_STOPPED -> {
                    val defaultSecs = if (switchDebugMode.isChecked) 30 else 1500
                    updateTimerUI(defaultSecs, isRunning = false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvTimer = findViewById(R.id.tvTimer)
        tvStatus = findViewById(R.id.tvStatus)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        switchDebugMode = findViewById(R.id.switchDebugMode)

        setupListeners()
    }

    private fun setupListeners() {
        btnStart.setOnClickListener {
            val duration = if (switchDebugMode.isChecked) 30 else 1500
            checkPermissionAndStart(duration)
        }

        btnStop.setOnClickListener {
            stopTimerService()
        }

        switchDebugMode.setOnCheckedChangeListener { _, isChecked ->
            if (!TimerService.isServiceRunning) {
                val secs = if (isChecked) 30 else 1500
                updateTimerDisplay(secs)
            }
        }
    }

    private fun checkPermissionAndStart(durationSeconds: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) {
                pendingStartDuration = durationSeconds
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }
        startTimerService(durationSeconds)
    }

    private fun startTimerService(durationSeconds: Int) {
        val intent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION_SECONDS, durationSeconds)
        }
        ContextCompat.startForegroundService(this, intent)
        updateTimerUI(durationSeconds, isRunning = true)
    }

    private fun stopTimerService() {
        val intent = Intent(this, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        startService(intent)

        val defaultSecs = if (switchDebugMode.isChecked) 30 else 1500
        updateTimerUI(defaultSecs, isRunning = false)
    }

    private fun navigateToFinishScreen() {
        val intent = Intent(this, FinishActivity::class.java)
        startActivity(intent)
    }

    private fun updateTimerUI(remainingSeconds: Int, isRunning: Boolean) {
        updateTimerDisplay(remainingSeconds)
        if (isRunning) {
            tvStatus.text = getString(R.string.status_focusing)
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_red))
            switchDebugMode.isEnabled = false
        } else {
            tvStatus.text = getString(R.string.status_ready)
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.accent_green))
            switchDebugMode.isEnabled = true
        }
    }

    private fun updateTimerDisplay(remainingSeconds: Int) {
        val minutes = maxOf(0, remainingSeconds) / 60
        val seconds = maxOf(0, remainingSeconds) % 60
        tvTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    override fun onResume() {
        super.onResume()
        registerTimerReceiver()
        queryServiceState()
    }

    override fun onPause() {
        super.onPause()
        unregisterTimerReceiver()
    }

    private fun queryServiceState() {
        if (TimerService.isServiceRunning) {
            val intent = Intent(this, TimerService::class.java).apply {
                action = TimerService.ACTION_QUERY
            }
            startService(intent)
        } else {
            val defaultSecs = if (switchDebugMode.isChecked) 30 else 1500
            updateTimerUI(defaultSecs, isRunning = false)
        }
    }

    private fun registerTimerReceiver() {
        if (!isReceiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(TimerService.BROADCAST_TICK)
                addAction(TimerService.BROADCAST_FINISH)
                addAction(TimerService.BROADCAST_STOPPED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.registerReceiver(
                    this,
                    timerReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(timerReceiver, filter)
            }
            isReceiverRegistered = true
        }
    }

    private fun unregisterTimerReceiver() {
        if (isReceiverRegistered) {
            try {
                unregisterReceiver(timerReceiver)
            } catch (e: Exception) {
                // Receiver might already be unregistered
            }
            isReceiverRegistered = false
        }
    }
}
