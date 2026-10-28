package com.example.hw5

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ProgressFragment : Fragment(R.layout.frag_progress) {

    private val viewModel: WorkViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val circularProgressBar = view.findViewById<CircularProgressBar>(R.id.circularProgressBar)
        val tvProgressStatus = view.findViewById<TextView>(R.id.tvProgressStatus)

        viewModel.workState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkState.Idle -> {
                    circularProgressBar.progress = 0
                    circularProgressBar.centerText = "0%"
                    tvProgressStatus.text = getString(R.string.status_idle)
                }
                is WorkState.Preparing -> {
                    circularProgressBar.progress = 0
                    circularProgressBar.centerText = "0%"
                    tvProgressStatus.text = getString(R.string.status_preparing)
                }
                is WorkState.Running -> {
                    circularProgressBar.progress = state.progress
                    circularProgressBar.centerText = "${state.progress}%"
                    tvProgressStatus.text = getString(R.string.status_running)
                }
                is WorkState.Completed -> {
                    circularProgressBar.progress = 100
                    circularProgressBar.centerText = "100%"
                    tvProgressStatus.text = getString(R.string.status_completed)
                }
                is WorkState.Cancelled -> {
                    circularProgressBar.progress = state.lastProgress
                    circularProgressBar.centerText = "${state.lastProgress}%"
                    tvProgressStatus.text = getString(R.string.status_cancelled)
                }
            }
        }
    }
}
