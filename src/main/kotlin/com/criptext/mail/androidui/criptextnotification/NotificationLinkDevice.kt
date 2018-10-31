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

class NotificationLinkDevice(override val ctx: Context): CriptextNotification(ctx) {

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
        val pushData = data as PushData.LinkDevice
        val okAction = Intent(ctx, MailboxActivity::class.java)
        okAction.action = LinkDeviceActionService.APPROVE
        okAction.addCategory(Intent.CATEGORY_LAUNCHER)
        okAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        okAction.putExtra("randomId", pushData.randomId)
        okAction.putExtra("deviceType", pushData.deviceType.ordinal)
        val okPendingIntent = PendingIntent.getActivity(ctx, 0, okAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val denyAction = Intent(ctx, LinkDeviceActionService::class.java)
        denyAction.action = LinkDeviceActionService.DENY
        denyAction.putExtra("notificationId", LINK_DEVICE_ID)
        denyAction.putExtra("randomId", pushData.randomId)
        val denyPendingIntent = PendingIntent.getService(ctx, 0, denyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)


        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val deviceIcon = when(pushData.deviceType){
            DeviceUtils.DeviceType.PC ->
                BitmapFactory.decodeResource(ctx.resources, R.drawable.device_pc_push)
            else -> BitmapFactory.decodeResource(ctx.resources, R.drawable.device_m_push)
        }


        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_LINK_DEVICE)
                .setContentTitle(pushData.title)
                .setContentText(pushData.body)
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_LINK_DEVICE)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .addAction(0, ctx.getString(R.string.push_approve), okPendingIntent)
                .addAction(0, ctx.getString(R.string.push_deny), denyPendingIntent)
                .setLargeIcon(deviceIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushData.body))



        return buildNotification(builder)
    }
}