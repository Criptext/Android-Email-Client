package com.criptext.mail.androidui

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
import com.criptext.mail.R
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.services.LinkDeviceActionService
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.services.NewMailActionService
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.Utility
import com.criptext.mail.utils.getLocalizedUIMessage

/**
 * Builds Notifications used in the Criptext App.
 * Created by gabriel on 7/25/17.
 */
class CriptextNotification(val ctx: Context) {

    private val storage = KeyValueStorage.SharedPrefs(ctx)

    companion object {
        //Actions for Notifications
        const val ACTION_OPEN = "open_activity"
        const val ACTION_INBOX = "open_thread"
        const val ACTION_LINK_DEVICE = "link_device"
        const val ACTION_ERROR = "error"

        //Channel Id's for the Notifications
        const val CHANNEL_ID_NEW_EMAIL = "new_email_channel"// The id of the channel.
        const val CHANNEL_ID_OPEN_EMAIL = "open_email_channel"// The id of the channel.
        const val CHANNEL_ID_LINK_DEVICE = "link_device_channel"// The id of the channel.
        const val CHANNEL_ID_ERROR = "error_channel"// The id of the channel.

        //Notification ID
        const val OPEN_ID = 0
        const val INBOX_ID = 1
        const val LINK_DEVICE_ID = 2
        const val ERROR_ID = 3
    }

    private fun buildNewMailNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.defaults = Notification.DEFAULT_VIBRATE
        notBuild.ledARGB = Color.YELLOW
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    private fun buildOpenMailboxNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.ledARGB = Color.YELLOW
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    private fun buildLinkDeviceNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.defaults = Notification.DEFAULT_VIBRATE
        notBuild.ledARGB = Color.YELLOW
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    fun createNewMailNotification(clickIntent: PendingIntent, title: String, body:String,
                                  metadataKey: Long, threadId: String,
                                  notificationId: Int)
            : Notification {

        val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, notCount + 1)

        val readAction = Intent(ctx, NewMailActionService::class.java)
        readAction.action = NewMailActionService.READ
        readAction.putExtra("notificationId", notificationId)
        readAction.putExtra("metadataKey", metadataKey)
        val readPendingIntent = PendingIntent.getService(ctx, notificationId, readAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val trashAction = Intent(ctx, NewMailActionService::class.java)
        trashAction.action = NewMailActionService.TRASH
        trashAction.putExtra("notificationId", notificationId)
        trashAction.putExtra("metadataKey", metadataKey)
        val trashPendingIntent = PendingIntent.getService(ctx, notificationId, trashAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val replyAction = Intent(ctx, MailboxActivity::class.java)
        replyAction.action = NewMailActionService.REPLY
        replyAction.addCategory(Intent.CATEGORY_LAUNCHER)
        replyAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        replyAction.putExtra("metadataKey", metadataKey)
        replyAction.putExtra(MessagingInstance.THREAD_ID, threadId)
        val replyPendingAction = PendingIntent.getActivity(ctx, 0, replyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_NEW_EMAIL)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
            .setSound(defaultSound)
            .setContentIntent(clickIntent)
            .setGroup(ACTION_INBOX)
            .setGroupSummary(false)
            .setSmallIcon(R.drawable.push_icon)
            .addAction(R.drawable.mail_opened, ctx.getString(R.string.push_read), readPendingIntent)
            .addAction(R.drawable.trash, ctx.getString(R.string.push_trash), trashPendingIntent)
            .addAction(R.drawable.reply, ctx.getString(R.string.push_reply), replyPendingAction)
            .setLargeIcon(Utility.getBitmapFromText(
                    title,
                    250,
                    250))
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))



        return buildNewMailNotification(builder)
    }

    fun createOpenMailboxNotification(clickIntent: PendingIntent, title: String, body:String,
                                      notificationId: Int)
            : Notification {

        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_OPEN_EMAIL)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_OPEN)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .setColor(Color.CYAN)
                .setLargeIcon(Utility.getBitmapFromText(
                        title,
                        250,
                        250))
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))



        return buildOpenMailboxNotification(builder)
    }

    fun createErrorNotification(title: UIMessage, body:UIMessage)
            : Notification {


        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_ERROR)
                .setContentTitle(ctx.getLocalizedUIMessage(title))
                .setContentText(ctx.getLocalizedUIMessage(body))
                .setAutoCancel(true)
                .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .setGroup(ACTION_ERROR)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .setColor(Color.CYAN)
                .setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getLocalizedUIMessage(body)))



        return buildOpenMailboxNotification(builder)
    }

    fun createLinkDeviceNotification(clickIntent: PendingIntent, title: String, body:String,
                                     randomId: String, deviceType: DeviceUtils.DeviceType,
                                      notificationId: Int, pushDataSource: PushDataSource)
            : Notification {

        val okAction = Intent(ctx, MailboxActivity::class.java)
        okAction.action = LinkDeviceActionService.APPROVE
        okAction.addCategory(Intent.CATEGORY_LAUNCHER)
        okAction.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        okAction.putExtra("randomId", randomId)
        okAction.putExtra("deviceType", deviceType.ordinal)
        val okPendingIntent = PendingIntent.getActivity(ctx, 0, okAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)

        val denyAction = Intent(ctx, LinkDeviceActionService::class.java)
        denyAction.action = LinkDeviceActionService.DENY
        denyAction.putExtra("notificationId", LINK_DEVICE_ID)
        denyAction.putExtra("randomId", randomId)
        val denyPendingIntent = PendingIntent.getService(ctx, 0, denyAction,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT)


        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val deviceIcon = when(deviceType){
            DeviceUtils.DeviceType.PC ->
                BitmapFactory.decodeResource(ctx.resources, R.drawable.device_pc_push)
            else -> BitmapFactory.decodeResource(ctx.resources, R.drawable.device_m_push)
        }


        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_LINK_DEVICE)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .setSound(defaultSound)
                .setContentIntent(clickIntent)
                .setGroup(ACTION_LINK_DEVICE)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .addAction(R.drawable.check, ctx.getString(R.string.push_approve), okPendingIntent)
                .addAction(R.drawable.x, ctx.getString(R.string.push_deny), denyPendingIntent)
                .setLargeIcon(deviceIcon)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))



        return buildLinkDeviceNotification(builder)
    }

    fun notify(id: Int, notification: Notification, group: String) {
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(getNotificationChannel(group))
        }
        notManager.notify(id, notification)
    }

    fun showHeaderNotification(title: String, icon: Int, group: String,
                               pendingIntent: PendingIntent? = null){
        val channelId = when(group){
            ACTION_OPEN -> CHANNEL_ID_OPEN_EMAIL
            ACTION_INBOX -> CHANNEL_ID_NEW_EMAIL
            ACTION_LINK_DEVICE -> CHANNEL_ID_LINK_DEVICE
            ACTION_ERROR -> CHANNEL_ID_ERROR
            else -> "DEFAULT_CHANNEL"
        }
        val builder = NotificationCompat.Builder(ctx, channelId)
                .setContentTitle(title)
                .setColor(Color.parseColor("#0091ff"))
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setGroup(group)


        if(pendingIntent != null) builder.setContentIntent(pendingIntent)

        val finalNotification = builder.build()

        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(getNotificationChannel(group))
        }
        when(group){
            ACTION_INBOX -> notManager.notify(INBOX_ID, finalNotification)
            ACTION_OPEN -> notManager.notify(OPEN_ID, finalNotification)
            ACTION_LINK_DEVICE -> notManager.notify(LINK_DEVICE_ID, finalNotification)
            ACTION_ERROR -> notManager.notify(ERROR_ID, finalNotification)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(group: String):NotificationChannel?{
        return when(group){
            ACTION_INBOX -> {
                val channelInfo = Pair("new_email_channel", ctx.getString(R.string.new_email_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
            }
            ACTION_OPEN -> {
                val channelInfo = Pair("open_email_channel", ctx.getString(R.string.email_open_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_LOW)
            }
            ACTION_LINK_DEVICE -> {
                val channelInfo = Pair("link_device_channel", ctx.getString(R.string.link_device_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
            }
            ACTION_ERROR -> {
                val channelInfo = Pair("error_channel", ctx.getString(R.string.error_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
            }
            else -> null

        }
    }


}