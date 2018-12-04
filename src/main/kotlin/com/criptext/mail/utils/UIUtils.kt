package com.criptext.mail.utils

import android.animation.ValueAnimator
import android.widget.TextView
import com.beardedhen.androidbootstrap.BootstrapProgressBar

object UIUtils{

    fun animationForProgressBar(progressBar: BootstrapProgressBar, progress: Int, progressBarNumber: TextView,
                                duration: Long): ValueAnimator{
        val anim = ValueAnimator.ofInt(progressBar.progress, progress)
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            progressBarNumber.text = (`val`).toString().plus("%")
            progressBar.progress = progress
        }
        anim.duration = duration
        return anim
    }
}