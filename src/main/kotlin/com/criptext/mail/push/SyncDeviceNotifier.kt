package com.criptext.mail.push

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationSyncDevice

sealed class SyncDeviceNotifier(val data: PushData.SyncDevice): Notifier {
    companion object {
        private val type = PushTypes.syncDevice
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postNotification(ctx: Context, isPostNougat: Boolean) {
        val cn = NotificationSyncDevice(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(notification.first, notification.second,
                CriptextNotification.ACTION_SYNC_DEVICE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postHeaderNotification(ctx: Context){
        val cn = NotificationSyncDevice(ctx)
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_SYNC_DEVICE)
    }

    protected abstract fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx, data.isPostNougat)
        }
    }

    class Open(data: PushData.SyncDevice, private val notificationId: Int): SyncDeviceNotifier(data) {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification> {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                null, data.isPostNougat)

            return Pair(notificationId, cn.createNotification(clickIntent = pendingIntent,
                    data = data,
                    notificationId = notificationId))

        }
    }

}