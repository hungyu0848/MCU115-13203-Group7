package com.example.hw5

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class StartFragment : Fragment(R.layout.frag_start) {

    private val viewModel: WorkViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnStart = view.findViewById<Button>(R.id.btnStart)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val tvStartStatus = view.findViewById<TextView>(R.id.tvStartStatus)

        btnStart.setOnClickListener {
            viewModel.startWork()
        }

        btnCancel.setOnClickListener {
            viewModel.cancelWork()
        }

        viewModel.workState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkState.Idle -> {
                    btnStart.isEnabled = true
                    btnCancel.isEnabled = false
                    tvStartStatus.text = getString(R.string.status_idle)
                }
                is WorkState.Preparing -> {
                    btnStart.isEnabled = false
                    btnCancel.isEnabled = true
                    tvStartStatus.text = getString(R.string.status_preparing)
                }
                is WorkState.Running -> {
                    btnStart.isEnabled = false
                    btnCancel.isEnabled = true
                    tvStartStatus.text = getString(R.string.status_running_percent, state.progress)
                }
                is WorkState.Completed -> {
                    btnStart.isEnabled = true
                    btnCancel.isEnabled = false
                    tvStartStatus.text = getString(R.string.status_completed)
                }
                is WorkState.Cancelled -> {
                    btnStart.isEnabled = true
                    btnCancel.isEnabled = false
                    tvStartStatus.text = getString(R.string.status_cancelled_percent, state.lastProgress)
                }
            }
        }
    }
}
