package com.criptext.mail.bgworker

import android.os.Handler
import java.lang.ref.WeakReference

/**
 * Created by gabriel on 5/14/18.
 */
interface RunnableThrottler {
    val interval: Long
    fun push(newRunnable: Runnable)
    fun cancel()

    open class Default(override val interval: Long): RunnableThrottler {
        private var queuedRunnable: Runnable? = null
        private val handler = Handler()

        private fun clearQueuedRunnable() {
            queuedRunnable?.run()
            queuedRunnable = null
        }

        override fun push(newRunnable: Runnable) {
            queuedRunnable = newRunnable
            val newRunnableWeakRef = WeakReference(newRunnable)
            handler.postDelayed({
                if (queuedRunnable === newRunnableWeakRef.get())
                    clearQueuedRunnable()
            }, interval)
        }

        override fun cancel() {
            queuedRunnable = null
        }
    }
}

