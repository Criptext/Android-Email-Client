package com.criptext.mail.push

import android.content.Context

/**
 * Created by gabriel on 8/21/17.
 */

interface Notifier {
    fun notifyPushEvent(ctx: Context)
}