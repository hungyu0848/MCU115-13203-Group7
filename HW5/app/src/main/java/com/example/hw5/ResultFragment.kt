package com.example.hw5

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

class ResultFragment : Fragment(R.layout.frag_result) {

    private val viewModel: WorkViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvResultStatus = view.findViewById<TextView>(R.id.tvResultStatus)

        viewModel.workState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is WorkState.Idle -> {
                    tvResultStatus.text = getString(R.string.status_idle)
                }
                is WorkState.Preparing -> {
                    tvResultStatus.text = getString(R.string.status_preparing)
                }
                is WorkState.Running -> {
                    tvResultStatus.text = getString(R.string.status_running)
                }
                is WorkState.Completed -> {
                    tvResultStatus.text = getString(R.string.status_completed)
                }
                is WorkState.Cancelled -> {
                    tvResultStatus.text = getString(R.string.status_cancelled)
                }
            }
        }
    }
}
