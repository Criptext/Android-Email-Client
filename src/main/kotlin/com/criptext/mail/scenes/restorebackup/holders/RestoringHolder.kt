package com.criptext.mail.scenes.restorebackup.holders

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.criptext.mail.R
import com.criptext.mail.androidui.progressdialog.IntervalTimer
import com.criptext.mail.scenes.restorebackup.RestoreBackupModel
import com.criptext.mail.utils.ui.ProgressBarAnimation

class RestoringHolder(val view: View, val model: RestoreBackupModel): BaseRestoreBackupHolder() {

    private val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
    private val progressBarNumber: TextView = view.findViewById(R.id.percentage_advanced)

    init {
        progressBar.progress = 0
        progressBarNumber.text = "0%"
    }

    fun setProgress(progress: Int, onFinish: (() -> Unit)?){
        val anim = ProgressBarAnimation(progressBar, progressBar.progress, progress, progressBarNumber, onFinish)
        anim.duration = 1000
        progressBar.startAnimation(anim)
    }

    fun localPercentageAnimation(){
        setProgress(100) {
            uiObserver?.onLocalProgressFinished()
        }
    }
}