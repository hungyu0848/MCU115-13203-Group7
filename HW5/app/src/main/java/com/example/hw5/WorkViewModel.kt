package com.example.hw5

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

sealed class WorkState {
    object Idle : WorkState()
    object Preparing : WorkState()
    data class Running(val progress: Int) : WorkState()
    object Completed : WorkState()
    data class Cancelled(val lastProgress: Int) : WorkState()
}

class WorkViewModel : ViewModel() {

    private val _workState = MutableLiveData<WorkState>(WorkState.Idle)
    val workState: LiveData<WorkState> = _workState

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile
    private var workerThread: Thread? = null

    @Volatile
    private var isCancelled = false

    fun startWork() {
        val current = _workState.value
        if (current is WorkState.Preparing || current is WorkState.Running) {
            return
        }

        mainHandler.removeCallbacksAndMessages(null)
        isCancelled = false
        _workState.value = WorkState.Preparing

        workerThread = Thread {
            try {
                // 1. Preparation phase (approx 1.5s)
                val prepInterval = 100
                var elapsedPrep = 0
                while (elapsedPrep < 1500) {
                    if (isCancelled || Thread.currentThread().isInterrupted) return@Thread
                    Thread.sleep(prepInterval.toLong())
                    elapsedPrep += prepInterval
                }

                if (isCancelled || Thread.currentThread().isInterrupted) return@Thread

                mainHandler.post {
                    if (!isCancelled) {
                        _workState.value = WorkState.Running(0)
                    }
                }

                // 2. Running phase (0% -> 100%)
                for (p in 1..100) {
                    if (isCancelled || Thread.currentThread().isInterrupted) return@Thread
                    Thread.sleep(100)
                    if (isCancelled || Thread.currentThread().isInterrupted) return@Thread

                    mainHandler.post {
                        if (!isCancelled) {
                            _workState.value = WorkState.Running(p)
                        }
                    }
                }

                if (isCancelled || Thread.currentThread().isInterrupted) return@Thread

                mainHandler.post {
                    if (!isCancelled) {
                        _workState.value = WorkState.Completed
                    }
                }
            } catch (_: InterruptedException) {
                // Thread interrupted on cancellation
            }
        }.apply {
            start()
        }
    }

    fun cancelWork() {
        val currentState = _workState.value
        if (currentState is WorkState.Preparing || currentState is WorkState.Running) {
            isCancelled = true
            workerThread?.interrupt()
            workerThread = null

            mainHandler.removeCallbacksAndMessages(null)

            val lastProgress = when (currentState) {
                is WorkState.Running -> currentState.progress
                else -> 0
            }
            _workState.value = WorkState.Cancelled(lastProgress)
        }
    }

    override fun onCleared() {
        super.onCleared()
        cancelWork()
        mainHandler.removeCallbacksAndMessages(null)
    }
}
