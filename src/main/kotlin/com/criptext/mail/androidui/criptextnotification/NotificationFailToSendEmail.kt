package com.criptext.mail.androidui.criptextnotification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import android.text.Html
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.PushData
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.*
import com.criptext.mail.api.Hosts
import com.criptext.mail.push.services.FailToSendEmailActionService
import com.criptext.mail.utils.compat.HtmlCompat


class NotificationFailToSendEmail(override val ctx: Context): CriptextNotification(ctx) {
    override fun updateNotification(notificationId: Int, data: PushData): Notification {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    val storage = KeyValueStorage.SharedPrefs(ctx = ctx)

    override fun buildNotification(builder: NotificationCompat.Builder): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL

        return notBuild
    }

    override fun createNotification(notificationId: Int, clickIntent: PendingIntent?,
                                    data: PushData): Notification {
        val pushData = data as PushData.FailToSendEmail

        val notCount = storage.getInt(KeyValueStorage.StringKey.FailToSendNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.FailToSendNotificationCount, notCount + 1)

        val retryAction = Intent(ctx, FailToSendEmailActionService::class.java)
        retryAction.action = FailToSendEmailActionService.RETRY
        val retryPendingIntent = PendingIntent.getService(ctx, notificationId, retryAction,0)

        val deleteAction = Intent(ctx, FailToSendEmailActionService::class.java)
        deleteAction.action = FailToSendEmailActionService.DISMISS
        val deletePendingIntent = PendingIntent.getService(ctx, notificationId, deleteAction,0)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val largeIcon = Utility.getCroppedBitmap(pushData.senderImage) ?: Utility.getBitmapFromText(
                pushData.name,
                250,
                250)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_FAIL_TO_SEND_EMAIL)
                .setContentText(ctx.getString(R.string.fail_to_send_notification_title))
                .setSubText(pushData.activeEmail)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_FAILED_EMAIL)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .addAction(0, ctx.getString(R.string.retry), retryPendingIntent)
                .addAction(0, ctx.getString(R.string.discard), deletePendingIntent)
                .setDeleteIntent(deletePendingIntent)
                .setLargeIcon(largeIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(
                    ctx.getLocalizedUIMessage(
                            UIMessage(
                                    R.string.fail_to_send_notification_message,
                                    arrayOf(pushData.name)
                            )
                        )
                    )
                )



        return buildNotification(builder)
    }
}