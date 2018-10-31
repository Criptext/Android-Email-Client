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

class NotificationError(val ctx: Context) {

    companion object {
        //Actions for Notifications
        const val ACTION_ERROR = "error"

        //Channel Id's for the Notifications
        const val CHANNEL_ID_ERROR = "error_channel"// The id of the channel.

        //Notification ID
        const val ERROR_ID = 3
    }

    private fun buildErrorNotification(builder: NotificationCompat.Builder): Notification {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder.color = Color.parseColor("#0091ff")

        val notBuild = builder.build()
        notBuild.ledARGB = Color.CYAN
        notBuild.flags = notBuild.flags or Notification.FLAG_AUTO_CANCEL
        notBuild.ledOnMS = 1000
        notBuild.ledOffMS = 1000

        return notBuild
    }

    fun createErrorNotification(clickIntent: PendingIntent, title: UIMessage, body:UIMessage)
            : Notification {


        val builder = NotificationCompat.Builder(ctx, CHANNEL_ID_ERROR)
                .setContentTitle(ctx.getLocalizedUIMessage(title))
                .setContentText(ctx.getLocalizedUIMessage(body))
                .setAutoCancel(true)
                .setContentIntent(clickIntent)
                .setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .setGroup(ACTION_ERROR)
                .setGroupSummary(false)
                .setSmallIcon(R.drawable.push_icon)
                .setColor(Color.CYAN)
                .setStyle(NotificationCompat.BigTextStyle().bigText(ctx.getLocalizedUIMessage(body)))



        return buildErrorNotification(builder)
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
            ACTION_ERROR -> notManager.notify(ERROR_ID, finalNotification)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotificationChannel(group: String):NotificationChannel?{
        return when(group){
            ACTION_ERROR -> {
                val channelInfo = Pair("error_channel", ctx.getString(R.string.error_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
            }
            else -> null

        }
    }


}