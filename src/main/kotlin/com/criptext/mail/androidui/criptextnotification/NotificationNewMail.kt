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
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
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
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getLocalizedUIMessage

class NotificationNewMail(override val ctx: Context): CriptextNotification(ctx) {

    val storage = KeyValueStorage.SharedPrefs(ctx = ctx)

    override fun buildNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.defaults = Notification.DEFAULT_VIBRATE
        notBuild.ledARGB = Color.CYAN
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    override fun createNotification(notificationId: Int, clickIntent: PendingIntent?,
                                    data: PushData): Notification {
        val pushData = data as PushData.NewMail
        val showEmailPreview = storage.getBool(KeyValueStorage.StringKey.ShowEmailPreview, true)

        val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, notCount + 1)

        val readAction = Intent(ctx, NewMailActionService::class.java)
        readAction.action = NewMailActionService.READ
        readAction.putExtra("notificationId", notificationId)
        readAction.putExtra("metadataKey", pushData.metadataKey)
        val readPendingIntent = PendingIntent.getService(ctx, notificationId, readAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val trashAction = Intent(ctx, NewMailActionService::class.java)
        trashAction.action = NewMailActionService.TRASH
        trashAction.putExtra("notificationId", notificationId)
        trashAction.putExtra("metadataKey", pushData.metadataKey)
        val trashPendingIntent = PendingIntent.getService(ctx, notificationId, trashAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val replyAction = Intent(ctx, MailboxActivity::class.java)
        replyAction.action = NewMailActionService.REPLY
        replyAction.addCategory(Intent.CATEGORY_LAUNCHER)
        replyAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        replyAction.putExtra("metadataKey", pushData.metadataKey)
        replyAction.putExtra(MessagingInstance.THREAD_ID, pushData.threadId)
        val replyPendingAction = PendingIntent.getActivity(ctx, 0, replyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val deleteAction = Intent(ctx, NewMailActionService::class.java)
        deleteAction.action = NewMailActionService.DELETE
        val deletePendingIntent = PendingIntent.getService(ctx, notificationId, deleteAction,0)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val preview = if(pushData.preview.length == 300) pushData.preview.plus("...") else pushData.preview
        val pushHtmlBody = "<span style='color:black;'>${pushData.body}</span><br>$preview"
        val pushBody = if(showEmailPreview)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                Html.fromHtml(pushHtmlBody, Html.FROM_HTML_MODE_LEGACY)
            else
                Html.fromHtml(pushHtmlBody)
        else
            pushData.body

        val pushHtmlText = "<span style='color:black;'>${pushData.body}</span>"
        val pushText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.fromHtml(pushHtmlText, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.fromHtml(pushHtmlText)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_NEW_EMAIL)
                .setContentTitle(pushData.title)
                .setContentText(pushText)
                .setSubText(pushData.activeEmail)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_INBOX)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .addAction(0, ctx.getString(R.string.push_read), readPendingIntent)
                .addAction(0, ctx.getString(R.string.push_trash), trashPendingIntent)
                .addAction(0, ctx.getString(R.string.push_reply), replyPendingAction)
                .setDeleteIntent(deletePendingIntent)
                .setLargeIcon(Utility.getBitmapFromText(
                        pushData.title,
                        250,
                        250))
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushBody))



        return buildNotification(builder)
    }
}