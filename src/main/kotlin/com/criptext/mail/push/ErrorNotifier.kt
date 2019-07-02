package com.criptext.mail.push

import android.app.Notification
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationError
import com.criptext.mail.utils.getLocalizedUIMessage

sealed class ErrorNotifier(val data: PushData.Error): Notifier {
    companion object {
        private val type = PushTypes.newMail
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postNotification(ctx: Context, isPostNougat: Boolean) {
        val cn = NotificationError(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(if(isPostNougat) type.requestCodeRandom() else type.requestCode(), notification, CriptextNotification.ACTION_ERROR)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun postHeaderNotification(ctx: Context){
        val cn = NotificationError(ctx)
        cn.showHeaderNotification(ctx.getLocalizedUIMessage(data.title), R.drawable.push_icon,
                CriptextNotification.ACTION_ERROR)
    }

    protected abstract fun buildNotification(ctx: Context, cn: NotificationError): Notification

    @RequiresApi(Build.VERSION_CODES.O)
    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx, data.isPostNougat)
        }
    }

    class Open(data: PushData.Error): ErrorNotifier(data) {

        @RequiresApi(Build.VERSION_CODES.O)
        override fun buildNotification(ctx: Context, cn: NotificationError): Notification {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                extraParam = null, isPostNougat = data.isPostNougat, account = null, domain = null)

            return cn.createNotification(clickIntent = pendingIntent,
                    data = data, notificationId = if(data.isPostNougat) type.requestCodeRandom() else type.requestCode())
        }
    }

}