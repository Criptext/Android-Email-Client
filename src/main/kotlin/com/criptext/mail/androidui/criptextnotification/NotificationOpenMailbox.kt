package com.criptext.mail.androidui.criptextnotification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import android.text.Html
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.PushData
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.*

class NotificationOpenMailbox(override val ctx: Context): CriptextNotification(ctx) {

    override fun buildNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL

        return notBuild
    }

    override fun createNotification(notificationId: Int, clickIntent: PendingIntent?,
                                    data: PushData): Notification {
        val pushData = data as PushData.OpenMailbox
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_OPEN_EMAIL)
                .setContentTitle(pushData.title)
                .setContentText(pushData.body)
                .setSubText(pushData.recipientId.plus("@${pushData.domain}"))
                .setAutoCancel(true)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_OPEN)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .setColor(Color.CYAN)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushData.body))

        return buildNotification(builder)
    }
}