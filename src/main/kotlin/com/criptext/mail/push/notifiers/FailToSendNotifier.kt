package com.criptext.mail.push.notifiers

import android.app.Notification
import android.content.Context
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationFailToSendEmail
import com.criptext.mail.androidui.criptextnotification.NotificationNewMail
import com.criptext.mail.push.ActivityIntentFactory
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes

sealed class FailToSendNotifier(val data: PushData.FailToSendEmail): Notifier {
    companion object {
        private val type = PushTypes.failedEmail
    }


    private fun postNotification(ctx: Context) {
        val cn = NotificationFailToSendEmail(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(notification.first, notification.second, CriptextNotification.ACTION_FAILED_EMAIL)
    }

    private fun postHeaderNotification(ctx: Context){
        val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, PushTypes.failedEmail,
                null, data.isPostNougat)
        val cn = NotificationFailToSendEmail(ctx)
        cn.showHeaderNotification(data.name, R.drawable.push_icon,
                CriptextNotification.ACTION_FAILED_EMAIL, pendingIntent)
    }

    protected abstract fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification>

    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx)
        }
    }

    override fun updatePushEvent(ctx: Context) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    class Single(data: PushData.FailToSendEmail, private val notificationId: Int): FailToSendNotifier(data) {

        override fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification> {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                    null, data.isPostNougat, null, null)

            return Pair(notificationId, cn.createNotification(clickIntent = pendingIntent, data = data,
                    notificationId = notificationId))

        }
    }

}