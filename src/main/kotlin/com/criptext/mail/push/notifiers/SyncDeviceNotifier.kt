package com.criptext.mail.push.notifiers

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationSyncDevice
import com.criptext.mail.push.ActivityIntentFactory
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes

sealed class SyncDeviceNotifier(val data: PushData.SyncDevice): Notifier {
    companion object {
        private val type = PushTypes.syncDevice
    }

    private fun postNotification(ctx: Context) {
        val cn = NotificationSyncDevice(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(notification.first, notification.second,
                CriptextNotification.ACTION_SYNC_DEVICE)
    }

    private fun postHeaderNotification(ctx: Context){
        val cn = NotificationSyncDevice(ctx)
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_SYNC_DEVICE)
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

    class Open(data: PushData.SyncDevice, private val notificationId: Int): SyncDeviceNotifier(data) {

        override fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification> {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                    null, data.isPostNougat)

            return Pair(notificationId, cn.createNotification(clickIntent = pendingIntent,
                    data = data,
                    notificationId = notificationId))

        }
    }

}