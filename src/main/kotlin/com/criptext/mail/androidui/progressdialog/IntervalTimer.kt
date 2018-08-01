package com.criptext.mail.androidui.progressdialog

import android.os.Handler

/**
 * Timer for periodic computations.
 * Created by gabriel on 7/10/17.
 */

internal class IntervalTimer {
    private val handler = Handler()
    private var cancelled = false

    fun start(intervals: Long, callback: Runnable) {
        handler.postDelayed({
            if (!cancelled) {
                callback.run()
                start(intervals, callback)
            }
        }, intervals)
    }

    fun stop() {
        cancelled = true
    }

    fun postDelayed(r: Runnable, delay: Long) {
        handler.postDelayed(r, delay)
    }

}
