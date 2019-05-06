package com.criptext.mail.androidui.criptextnotification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.push.PushData
import com.criptext.mail.push.services.SyncDeviceActionService
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.EmailAddressUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage

class NotificationSyncDevice(override val ctx: Context): CriptextNotification(ctx) {

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
        val pushData = data as PushData.SyncDevice
        val okAction = Intent(ctx, MailboxActivity::class.java)
        okAction.action = SyncDeviceActionService.APPROVE
        okAction.addCategory(Intent.CATEGORY_LAUNCHER)
        okAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        okAction.putExtra("notificationId", notificationId)
        okAction.putExtra("randomId", pushData.randomId)
        okAction.putExtra("deviceId", pushData.deviceId)
        okAction.putExtra("deviceName", pushData.deviceName)
        okAction.putExtra("deviceType", pushData.deviceType.ordinal)
        okAction.putExtra("version", pushData.syncFileVersion)
        okAction.putExtra("account", pushData.recipientId)
        val okPendingIntent = PendingIntent.getActivity(ctx, notificationId, okAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val denyAction = Intent(ctx, SyncDeviceActionService::class.java)
        denyAction.action = SyncDeviceActionService.DENY
        denyAction.putExtra("notificationId", notificationId)
        denyAction.putExtra("randomId", pushData.randomId)
        denyAction.putExtra("version", pushData.syncFileVersion)
        denyAction.putExtra("account", pushData.recipientId)
        val denyPendingIntent = PendingIntent.getService(ctx, notificationId, denyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)


        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val deviceIcon = when(pushData.deviceType){
            DeviceUtils.DeviceType.PC, DeviceUtils.DeviceType.MacStore, DeviceUtils.DeviceType.MacInstaller,
            DeviceUtils.DeviceType.WindowsInstaller, DeviceUtils.DeviceType.WindowsStore,
            DeviceUtils.DeviceType.LinuxInstaller -> BitmapFactory.decodeResource(ctx.resources, R.drawable.device_pc_push)
            else -> BitmapFactory.decodeResource(ctx.resources, R.drawable.device_m_push)
        }


        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_SYNC_DEVICE)
                .setContentTitle(ctx.getLocalizedUIMessage(UIMessage(R.string.push_link_error_title)))
                .setContentText(
                        ctx.getLocalizedUIMessage(UIMessage(R.string.push_link_device_message,
                                arrayOf(data.deviceName)))
                )
                .setSubText(pushData.recipientId.plus("@${pushData.domain}"))
                .setAutoCancel(true)
                .setSound(defaultSound)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_SYNC_DEVICE)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .addAction(0, ctx.getString(R.string.push_approve), okPendingIntent)
                .addAction(0, ctx.getString(R.string.push_deny), denyPendingIntent)
                .setLargeIcon(deviceIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(pushData.body))



        return buildNotification(builder)
    }
}