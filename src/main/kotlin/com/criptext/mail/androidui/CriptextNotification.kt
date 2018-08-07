package com.criptext.mail.androidui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import com.criptext.mail.BaseActivity
import com.criptext.mail.R
import com.criptext.mail.utils.Utility

/**
 * Builds Notifications used in the Criptext App.
 * Created by gabriel on 7/25/17.
 */
class CriptextNotification(val ctx: Context) {

    val name: String = ctx.getString(R.string.new_email_notification)// The user-visible name of the channel.
    private val mChannel:NotificationChannel? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_HIGH)
    } else {
        null
    }

    companion object {
        val ACTION_OPEN = "open_activity"
        val ACTION_INBOX = "open_thread"
        val CHANNEL_ID = "new_email_channel"// The id of the channel.
    }

    private fun buildNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0276a9")

        val notBuild = builder.build()
        notBuild.defaults = Notification.DEFAULT_VIBRATE
        notBuild.ledARGB = Color.YELLOW
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    fun buildNewMailNotification(clickIntent: PendingIntent, title: String, body:String,
                                 notificationId: Int)
            : Notification {

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
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



        return buildNotification(builder)
    }

    fun notify(id: Int, notification: Notification) {
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(mChannel)
        }
        notManager.notify(id, notification)
    }

    fun showHeaderNotification(title: String, icon: Int, group: String){
        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setContentTitle(title)
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setGroup(group)
                .build()
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(mChannel)
        }
        notManager.notify(0, builder)
    }


}