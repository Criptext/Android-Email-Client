package com.email.scenes.keygeneration

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
    private val progressBar: ProgressBar
    private val percentageAdvanced: TextView
    private val timer = IntervalTimer()

    fun updateProgress(progress: Int) {
        percentageAdvanced.text = progress.toString()
        progressBar.progress = progress
    }

    fun stopTimer() {
        timer.stop()
    }

    init {
        progressBar = view.findViewById(R.id.progressBar)
        percentageAdvanced = view.findViewById(R.id.percentage_advanced)
        var counter = 0
        timer.start(intervalDuration, Runnable {
            updateProgress(counter++)
            Thread.sleep(100)
            checkProgress(counter)
        })
    }
}
