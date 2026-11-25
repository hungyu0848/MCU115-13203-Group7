package com.example.hw7

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.Locale
import kotlin.concurrent.thread

/**
 * 智慧番茄鐘倒數服務 (TimerService)
 * 需求重點：
 * 1. 背景執行計時，使用基於單調時鐘 SystemClock.elapsedRealtime() 的截止時間計算。
 * 2. onStartCommand 回傳 START_NOT_STICKY。
 * 3. 動態註冊 ACTION_SCREEN_ON：亮屏彈出實際剩餘時間 Toast。
 * 4. 動態註冊 ACTION_HEADSET_PLUG：監聽耳機拔插控制專注音效。
 * 5. 使用前景服務 Notification 以相容最新 Android 背景限制。
 */
class TimerService : Service() {

    companion object {
        const val ACTION_START = "com.example.hw7.ACTION_START"
        const val ACTION_STOP = "com.example.hw7.ACTION_STOP"
        const val ACTION_QUERY = "com.example.hw7.ACTION_QUERY"

        const val EXTRA_DURATION_SECONDS = "extra_duration_seconds"

        const val BROADCAST_TICK = "com.example.hw7.TIMER_TICK"
        const val BROADCAST_FINISH = "com.example.hw7.TIMER_FINISH"
        const val BROADCAST_STOPPED = "com.example.hw7.TIMER_STOPPED"

        const val EXTRA_REMAINING_SECONDS = "extra_remaining_seconds"
        const val EXTRA_TOTAL_SECONDS = "extra_total_seconds"
        const val EXTRA_IS_RUNNING = "extra_is_running"

        const val NOTIFICATION_ID = 2001
        const val FINISH_NOTIFICATION_ID = 2002
        const val CHANNEL_ID = "pomodoro_focus_service_channel"

        @Volatile
        var isServiceRunning: Boolean = false
            private set

        @Volatile
        var currentRemainingSeconds: Int = 1500
            private set

        @Volatile
        var currentTotalSeconds: Int = 1500
            private set
    }

    private var countdownThread: Thread? = null
    @Volatile
    private var isTimerRunning = false
    private var targetEndTimeMs: Long = 0L
    private var totalSeconds = 1500

    private var screenReceiver: ScreenReceiver? = null
    private var headsetReceiver: HeadsetReceiver? = null
    private var focusAudioPlayer: FocusAudioPlayer? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        focusAudioPlayer = FocusAudioPlayer()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START
        when (action) {
            ACTION_START -> {
                val duration = intent?.getIntExtra(EXTRA_DURATION_SECONDS, 1500) ?: 1500
                startTimer(duration)
            }
            ACTION_STOP -> {
                stopTimer()
            }
            ACTION_QUERY -> {
                broadcastState()
            }
        }

        // 依據作業需求回傳 START_NOT_STICKY
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startTimer(durationSeconds: Int) {
        stopCountdownThread()

        totalSeconds = durationSeconds
        currentTotalSeconds = durationSeconds
        isTimerRunning = true
        isServiceRunning = true

        targetEndTimeMs = SystemClock.elapsedRealtime() + (durationSeconds * 1000L)

        // 啟動前景服務通知
        val initialNotification = buildNotification(durationSeconds)
        startForeground(NOTIFICATION_ID, initialNotification)

        // 動態註冊 ACTION_SCREEN_ON 系統廣播
        registerScreenReceiver()

        // 動態註冊 ACTION_HEADSET_PLUG 系統廣播
        registerHeadsetReceiver()

        // 啟動音效播放器
        focusAudioPlayer?.start()

        // 建立背景計時執行緒 (避免阻塞 UI 執行緒)
        countdownThread = thread(start = true, name = "PomodoroTimerThread") {
            var lastBroadcastSeconds = -1

            while (isTimerRunning) {
                val now = SystemClock.elapsedRealtime()
                val remainingMs = targetEndTimeMs - now
                val remainingSec = maxOf(0, ((remainingMs + 999) / 1000).toInt())

                currentRemainingSeconds = remainingSec

                if (remainingSec != lastBroadcastSeconds) {
                    lastBroadcastSeconds = remainingSec
                    sendTickBroadcast(remainingSec, totalSeconds)
                    updateNotification(remainingSec)
                }

                if (remainingSec <= 0) {
                    onTimerFinished()
                    break
                }

                try {
                    Thread.sleep(300)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
        isServiceRunning = false
        currentRemainingSeconds = totalSeconds

        stopCountdownThread()
        unregisterScreenReceiver()
        unregisterHeadsetReceiver()

        focusAudioPlayer?.stop()

        sendBroadcast(Intent(BROADCAST_STOPPED).apply {
            setPackage(packageName)
        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun onTimerFinished() {
        isTimerRunning = false
        isServiceRunning = false
        currentRemainingSeconds = 0

        unregisterScreenReceiver()
        unregisterHeadsetReceiver()
        focusAudioPlayer?.stop()

        // 發送倒數完成廣播
        sendBroadcast(Intent(BROADCAST_FINISH).apply {
            setPackage(packageName)
        })

        // 發送完成通知，點擊可開啟 FinishActivity
        showCompletionNotification()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
    }

    private fun stopCountdownThread() {
        countdownThread?.interrupt()
        countdownThread = null
    }

    private fun sendTickBroadcast(remainingSeconds: Int, totalSeconds: Int) {
        val intent = Intent(BROADCAST_TICK).apply {
            putExtra(EXTRA_REMAINING_SECONDS, remainingSeconds)
            putExtra(EXTRA_TOTAL_SECONDS, totalSeconds)
            putExtra(EXTRA_IS_RUNNING, true)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun broadcastState() {
        val intent = Intent(if (isTimerRunning) BROADCAST_TICK else BROADCAST_STOPPED).apply {
            putExtra(EXTRA_REMAINING_SECONDS, currentRemainingSeconds)
            putExtra(EXTRA_TOTAL_SECONDS, currentTotalSeconds)
            putExtra(EXTRA_IS_RUNNING, isTimerRunning)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun registerScreenReceiver() {
        if (screenReceiver == null) {
            screenReceiver = ScreenReceiver()
            val filter = IntentFilter(Intent.ACTION_SCREEN_ON)
            registerReceiver(screenReceiver, filter)
        }
    }

    private fun unregisterScreenReceiver() {
        screenReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            screenReceiver = null
        }
    }

    private fun registerHeadsetReceiver() {
        if (headsetReceiver == null) {
            headsetReceiver = HeadsetReceiver()
            val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
            registerReceiver(headsetReceiver, filter)
        }
    }

    private fun unregisterHeadsetReceiver() {
        headsetReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: Exception) {
                // Ignore if not registered
            }
            headsetReceiver = null
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(remainingSeconds: Int): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeStr = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getString(R.string.title_main))
            .setContentText("Focusing... $timeStr")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification(remainingSeconds: Int) {
        val notification = buildNotification(remainingSeconds)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification() {
        val finishIntent = Intent(this, FinishActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            1,
            finishIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_timer)
            .setContentTitle(getString(R.string.title_finish))
            .setContentText("00:00 - 點擊返回查看完成畫面")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(FINISH_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()
        focusAudioPlayer?.release()
    }

    /**
     * 動態廣播接收器：監聽 ACTION_SCREEN_ON (當使用者重新開啟螢幕時)
     */
    inner class ScreenReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_ON && isTimerRunning) {
                val remaining = currentRemainingSeconds
                val mins = remaining / 60
                val secs = remaining % 60

                val msg = if (mins > 0) {
                    "保持專注！還有 $mins 分鐘"
                } else {
                    "保持專注！還有 $secs 秒"
                }
                Toast.makeText(context.applicationContext, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * 動態廣播接收器：監聽 ACTION_HEADSET_PLUG (耳機拔插)
     */
    inner class HeadsetReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_HEADSET_PLUG) {
                val state = intent.getIntExtra("state", -1)
                when (state) {
                    0 -> { // 耳機拔除
                        focusAudioPlayer?.pause()
                        Toast.makeText(
                            context.applicationContext,
                            "耳機已拔除，專注音效已暫停",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    1 -> { // 耳機插入
                        if (isTimerRunning) {
                            focusAudioPlayer?.resume()
                            Toast.makeText(
                                context.applicationContext,
                                "耳機已插入，專注音效已恢復",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * 專注音效產生/控制器
     * 使用 AudioTrack 產生沉浸式專注音效 (Soft 440Hz Sine Tone Wave)，避免外部檔缺失。
     */
    inner class FocusAudioPlayer {
        private var audioTrack: AudioTrack? = null
        @Volatile
        private var isPlaying = false
        private var audioThread: Thread? = null

        fun start() {
            if (isPlaying) return
            isPlaying = true
            audioThread = thread(start = true, name = "FocusAudioThread") {
                try {
                    val sampleRate = 44100
                    val bufferSize = AudioTrack.getMinBufferSize(
                        sampleRate,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )
                    audioTrack = AudioTrack.Builder()
                        .setAudioAttributes(
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                        )
                        .setAudioFormat(
                            AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(sampleRate)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build()
                        )
                        .setBufferSizeInBytes(bufferSize)
                        .build()

                    audioTrack?.play()

                    val buffer = ShortArray(bufferSize)
                    var angle = 0.0
                    val frequency = 220.0 // Soft relaxing tone A3
                    val volume = 0.05 // Very gentle background level

                    while (isPlaying) {
                        for (i in buffer.indices) {
                            buffer[i] = (Math.sin(angle) * Short.MAX_VALUE * volume).toInt().toShort()
                            angle += 2.0 * Math.PI * frequency / sampleRate
                            if (angle > 2.0 * Math.PI) angle -= 2.0 * Math.PI
                        }
                        audioTrack?.write(buffer, 0, buffer.size)
                    }
                } catch (e: Exception) {
                    // Graceful handling if audio device unavailable
                }
            }
        }

        fun pause() {
            try {
                audioTrack?.pause()
            } catch (e: Exception) { }
        }

        fun resume() {
            try {
                if (isPlaying) {
                    audioTrack?.play()
                }
            } catch (e: Exception) { }
        }

        fun stop() {
            isPlaying = false
            audioThread?.interrupt()
            audioThread = null
            try {
                audioTrack?.stop()
                audioTrack?.release()
            } catch (e: Exception) { }
            audioTrack = null
        }

        fun release() {
            stop()
        }
    }
}
