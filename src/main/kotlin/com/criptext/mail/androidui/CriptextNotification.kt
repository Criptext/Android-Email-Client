package com.criptext.mail.androidui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.criptext.mail.R
import com.criptext.mail.utils.Utility

/**
 * Builds Notifications used in the Criptext App.
 * Created by gabriel on 7/25/17.
 */
class CriptextNotification(val ctx: Context) {

    companion object {
        val ACTION_OPEN = "open_activity"
        val ACTION_INBOX = "open_thread"
        val CHANNEL_ID_NEW_EMAIL = "new_email_channel"// The id of the channel.
        val CHANNEL_ID_OPEN_EMAIL = "open_email_channel"// The id of the channel.
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

    fun createNewMailNotification(clickIntent: PendingIntent, title: String, body:String,
                                  notificationId: Int)
            : Notification {

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
            .setLargeIcon(Utility.getBitmapFromText(
                    title,
                    title[0].toString().toUpperCase(),
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
                        title[0].toString().toUpperCase(),
                        250,
                        250))
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))



        return buildOpenMailboxNotification(builder)
    }

    fun notify(id: Int, notification: Notification, group: String) {
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(getNotificationChannel(group))
        }
        notManager.notify(id, notification)
    }

    fun showHeaderNotification(title: String, icon: Int, group: String){
        val channelId = when(group){
            ACTION_OPEN -> CHANNEL_ID_OPEN_EMAIL
            ACTION_INBOX -> CHANNEL_ID_NEW_EMAIL
            else -> "DEFAULT_CHANNEL"
        }
        val builder = NotificationCompat.Builder(ctx, channelId)
                .setContentTitle(title)
                .setColor(Color.parseColor("#0091ff"))
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setGroup(group)
                .build()
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(getNotificationChannel(group))
        }
        when(group){
            ACTION_INBOX -> notManager.notify(0, builder)
            ACTION_OPEN -> notManager.notify(1, builder)
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
            else -> null

        }
    }


}