package com.criptext.mail.push.notifiers

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationOpenMailbox
import com.criptext.mail.push.ActivityIntentFactory
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes

/**
 * Created by gabriel on 8/21/17.
 */

sealed class OpenMailboxNotifier(val data: PushData.OpenMailbox): Notifier {
    companion object {
        private val type = PushTypes.newMail
    }

    private fun postNotification(ctx: Context, isPostNougat: Boolean) {
        val cn = NotificationOpenMailbox(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(if(isPostNougat) type.requestCodeRandom() else type.requestCode(), notification, CriptextNotification.ACTION_OPEN)
    }

    private fun postHeaderNotification(ctx: Context){
        val cn = NotificationOpenMailbox(ctx)
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_OPEN)
    }

    protected abstract fun buildNotification(ctx: Context, cn: CriptextNotification): Notification

    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx, data.isPostNougat)
        }
    }

    override fun updatePushEvent(ctx: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Open(data: PushData.OpenMailbox): OpenMailboxNotifier(data) {

        override fun buildNotification(ctx: Context, cn: CriptextNotification): Notification {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                    null, data.isPostNougat)

            return cn.createNotification(clickIntent = pendingIntent,
                    data = data,
                    notificationId = if(data.isPostNougat) type.requestCodeRandom() else type.requestCode())

        }
    }

}