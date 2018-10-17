package com.criptext.mail.utils

import android.animation.ValueAnimator
import android.widget.ProgressBar
import android.widget.TextView

object UIUtils{
    fun animationForProgressBar(progressBar: ProgressBar, progress: Int, progressBarNumber: TextView,
                                duration: Long): ValueAnimator{
        val anim = ValueAnimator.ofInt(progressBar.measuredWidth, (progress * 9))
        anim.addUpdateListener { valueAnimator ->
            val `val` = valueAnimator.animatedValue as Int
            val layoutParams = progressBar.layoutParams
            layoutParams.width = `val`
            progressBarNumber.text = (`val`/9).toString().plus("%")
            progressBar.layoutParams = layoutParams
        }
        anim.duration = duration
        return anim
    }
}