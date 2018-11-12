package com.criptext.mail.androidui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.criptext.mail.R
import com.criptext.mail.push.PushData

/**
 * Builds Notifications used in the Criptext App.
 * Created by gabriel on 7/25/17.
 */
abstract class CriptextNotification(open val ctx: Context) {

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

    abstract fun buildNotification(builder: NotificationCompat.Builder): Notification
    abstract fun createNotification(notificationId: Int, clickIntent: PendingIntent?, data: PushData): Notification

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
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
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