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
import com.criptext.mail.utils.compat.HtmlCompat


class NotificationNewMail(override val ctx: Context): CriptextNotification(ctx) {

    val storage = KeyValueStorage.SharedPrefs(ctx = ctx)

    override fun buildNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL

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
        readAction.putExtra("account", pushData.account)
        readAction.putExtra("metadataKey", pushData.metadataKey)
        val readPendingIntent = PendingIntent.getService(ctx, notificationId, readAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val trashAction = Intent(ctx, NewMailActionService::class.java)
        trashAction.action = NewMailActionService.TRASH
        trashAction.putExtra("notificationId", notificationId)
        trashAction.putExtra("account", pushData.account)
        trashAction.putExtra("metadataKey", pushData.metadataKey)
        val trashPendingIntent = PendingIntent.getService(ctx, notificationId, trashAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val replyAction = Intent(ctx, MailboxActivity::class.java)
        replyAction.action = NewMailActionService.REPLY
        replyAction.addCategory(Intent.CATEGORY_LAUNCHER)
        replyAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        replyAction.putExtra("metadataKey", pushData.metadataKey)
        replyAction.putExtra("account", pushData.account)
        replyAction.putExtra(MessagingInstance.THREAD_ID, pushData.threadId)
        val replyPendingAction = PendingIntent.getActivity(ctx, 0, replyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val deleteAction = Intent(ctx, NewMailActionService::class.java)
        deleteAction.action = NewMailActionService.DELETE
        val deletePendingIntent = PendingIntent.getService(ctx, notificationId, deleteAction,0)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val preview = if(pushData.preview.isEmpty() && !pushData.hasInlineImages)
            "<i>" + ctx.getLocalizedUIMessage(UIMessage(R.string.nocontent)) + "</i>"
        else {
            if (pushData.preview.length == 300) pushData.preview.plus("...") else pushData.preview
        }

        val subject = if(pushData.subject.isEmpty()) ctx.getLocalizedUIMessage(UIMessage(R.string.nosubject)) else pushData.subject

        val pushHtmlBody = "<span style='color:black;'>$subject</span><br>$preview"
        val pushBody = if(showEmailPreview)
            HtmlCompat.fromHtml(pushHtmlBody)
        else
            subject

        val pushHtmlText = "<span style='color:black;'>$subject</span>"
        val pushText = HtmlCompat.fromHtml(pushHtmlText)

        val largeIcon = Utility.getCroppedBitmap(pushData.senderImage) ?: Utility.getBitmapFromText(
                pushData.name,
                250,
                250)

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_NEW_EMAIL)
                .setContentTitle(pushData.name)
                .setContentText(pushText)
                .setSubText(pushData.account.plus(EmailAddressUtils.CRIPTEXT_DOMAIN_SUFFIX))
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
                .setLargeIcon(largeIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushBody))



        return buildNotification(builder)
    }
}