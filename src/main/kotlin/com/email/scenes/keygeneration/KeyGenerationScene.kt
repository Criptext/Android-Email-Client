package com.email.scenes.keygeneration

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.email.R
import com.email.androidui.progressdialog.IntervalTimer

/**
 * Created by sebas on 2/28/18.
 */

class KeyGenerationHolder(
        private val view: View,
        private val checkProgress: (progress: Int) -> Unit,
        intervalDuration: Long
) {

    private val res = view.context.resources
    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    private val percentageAdvanced: TextView
    private val timer = IntervalTimer()
    var progress = 0

    fun updateProgress(progress: Int) {
        this.progress = progress
        percentageAdvanced.text = this.progress.toString()
        progressBar.progress = this.progress
    }

    fun stopTimer() {
        timer.stop()
    }

    init {
        percentageAdvanced = view.findViewById(R.id.percentage_advanced)
        timer.start(intervalDuration, Runnable {
            val progress = this.progress + 1
            updateProgress(progress)
            checkProgress(progress)
        })
    }
}
