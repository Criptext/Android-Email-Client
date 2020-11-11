package com.criptext.mail.utils.ui

import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.ProgressBar
import android.widget.TextView


class ProgressBarAnimation(private val progressBar: ProgressBar, private val from: Int,
                           private val to: Int, private val progressBarNumber: TextView?,
                           private val onFinish: (() -> Unit)?) : Animation() {
    override fun applyTransformation(interpolatedTime: Float, t: Transformation?) {
        super.applyTransformation(interpolatedTime, t)
        val value = from + (to - from) * interpolatedTime
        progressBar.progress = value.toInt()
        progressBarNumber?.text = value.toInt().toString().plus("%")
        if(value.toInt() >= to)
            onFinish?.let { it() }
    }

}