package com.criptext.mail.push.notifiers

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationJobBackupProgress
import com.criptext.mail.androidui.criptextnotification.NotificationOpenMailbox
import com.criptext.mail.push.ActivityIntentFactory
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes

sealed class JobBackupNotifier(val data: PushData.JobBackup): Notifier {
    companion object {
        private val type = PushTypes.jobBackup
    }

    private fun postNotification(ctx: Context) {
        val cn = NotificationJobBackupProgress(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(notification.first, notification.second, CriptextNotification.ACTION_JOB_BACKUP)
    }

    private fun updateNotification(ctx: Context) {
        val cn = NotificationJobBackupProgress(ctx)
        val notification = updateNotification(ctx, cn)
        cn.notify(notification.first, notification.second, CriptextNotification.ACTION_JOB_BACKUP)
    }

    private fun postHeaderNotification(ctx: Context){
        val cn = NotificationJobBackupProgress(ctx)
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_JOB_BACKUP)
    }

    protected abstract fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification>
    protected abstract fun updateNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification>

    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx)
        }
    }

    override fun updatePushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            updateNotification(ctx)
        }
    }

    class Open(data: PushData.JobBackup, private val notificationId: Int): JobBackupNotifier(data) {

        override fun buildNotification(ctx: Context, cn: CriptextNotification) : Pair<Int, Notification> {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                    null, data.isPostNougat)

            return Pair(notificationId, cn.createNotification(clickIntent = pendingIntent,
                    data = data,
                    notificationId = notificationId))

        }

        override fun updateNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification> {
            return Pair(notificationId, cn.updateNotification(data = data,
                    notificationId = notificationId))
        }
    }

}