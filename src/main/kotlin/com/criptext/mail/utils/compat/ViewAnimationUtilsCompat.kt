package com.criptext.mail.utils.compat

import android.animation.Animator
import android.os.Build
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator

class ViewAnimationUtilsCompat{
    companion object {
        fun createCircularReveal(view: View, centerX: Int, centerY: Int, startRadius: Float, endRadius: Float): Animator? {
            var circularReveal: Animator? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                circularReveal = ViewAnimationUtils.createCircularReveal(
                        view,
                        centerX,
                        centerY,
                        startRadius,
                        endRadius)
                circularReveal.duration = 500
                circularReveal.interpolator = AccelerateInterpolator()
            }
            return circularReveal
        }
    }
}