package com.criptext.mail.androidui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.criptext.mail.R
import com.criptext.mail.push.PushData
import com.criptext.mail.push.services.HeaderActionService
import com.criptext.mail.push.services.NewMailActionService

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
        const val ACTION_SYNC_DEVICE = "sync_device"
        const val ACTION_ERROR = "error"
        const val ACTION_JOB_BACKUP = "job_backup"

        //Channel Id's for the Notifications
        const val CHANNEL_ID_NEW_EMAIL = "new_email_channel"// The id of the channel.
        const val CHANNEL_ID_OPEN_EMAIL = "open_email_channel"// The id of the channel.
        const val CHANNEL_ID_LINK_DEVICE = "link_device_channel"// The id of the channel.
        const val CHANNEL_ID_SYNC_DEVICE = "sync_device_channel"// The id of the channel.
        const val CHANNEL_ID_ERROR = "error_channel"// The id of the channel.
        const val CHANNEL_ID_JOB_BACKUP = "job_backup_service_channel"// The id of the channel.

        //Notification ID
        const val OPEN_ID = 0
        const val INBOX_ID = 1
        const val LINK_DEVICE_ID = 2
        const val SYNC_DEVICE_ID = 4
        const val ERROR_ID = 3
        const val JOB_BACKUP_ID = 100

        fun getChannelId(action: String): String {
            return when(action){
                ACTION_OPEN -> CHANNEL_ID_OPEN_EMAIL
                ACTION_INBOX -> CHANNEL_ID_NEW_EMAIL
                ACTION_LINK_DEVICE -> CHANNEL_ID_LINK_DEVICE
                ACTION_SYNC_DEVICE -> CHANNEL_ID_SYNC_DEVICE
                ACTION_ERROR -> CHANNEL_ID_ERROR
                ACTION_JOB_BACKUP -> CHANNEL_ID_JOB_BACKUP
                else -> "DEFAULT_CHANNEL"
            }
        }

        fun getNotificationId(action: String): Int {
            return when(action){
                ACTION_OPEN -> OPEN_ID
                ACTION_INBOX -> INBOX_ID
                ACTION_LINK_DEVICE -> LINK_DEVICE_ID
                ACTION_SYNC_DEVICE -> LINK_DEVICE_ID
                ACTION_ERROR -> ERROR_ID
                ACTION_JOB_BACKUP -> JOB_BACKUP_ID
                else -> -1
            }
        }
    }

    abstract fun buildNotification(builder: NotificationCompat.Builder): Notification
    abstract fun createNotification(notificationId: Int, clickIntent: PendingIntent?, data: PushData): Notification
    abstract fun updateNotification(notificationId: Int, data: PushData): Notification

    fun notify(id: Int, notification: Notification, group: String) {
        val notManager = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notManager.createNotificationChannel(getNotificationChannel(group))
        }
        notManager.notify(id, notification)
    }

    fun showHeaderNotification(title: String, icon: Int, group: String,
                               pendingIntent: PendingIntent? = null){
        val channelId = getChannelId(group)
        val builder = NotificationCompat.Builder(ctx, channelId)
                .setContentTitle(title)
                .setColor(Color.parseColor("#0091ff"))
                .setSmallIcon(icon)
                .setAutoCancel(true)
                .setGroupSummary(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroup(group)

        val deleteAction = Intent(ctx, HeaderActionService::class.java)
        deleteAction.action = group
        val deletePendingIntent = PendingIntent.getService(ctx, getNotificationId(group), deleteAction,0)
        builder.setDeleteIntent(deletePendingIntent)

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
            ACTION_SYNC_DEVICE -> notManager.notify(LINK_DEVICE_ID, finalNotification)
            ACTION_ERROR -> notManager.notify(ERROR_ID, finalNotification)
            ACTION_JOB_BACKUP -> notManager.notify(JOB_BACKUP_ID, finalNotification)
        }

    }

    private fun getNotificationChannel(group: String):NotificationChannel {
        return when(group){
            ACTION_INBOX -> {
                val channelInfo = Pair(CHANNEL_ID_NEW_EMAIL, ctx.getString(R.string.new_email_notification))
                val not = NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
                not.lightColor = Color.CYAN
                not.enableLights(true)
                not.enableVibration(true)
                not
            }
            ACTION_OPEN -> {
                val channelInfo = Pair(CHANNEL_ID_OPEN_EMAIL, ctx.getString(R.string.email_open_notification))
                val not = NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_LOW)
                not
            }
            ACTION_LINK_DEVICE -> {
                val channelInfo = Pair(CHANNEL_ID_LINK_DEVICE, ctx.getString(R.string.link_device_notification))
                val not = NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
                not.lightColor = Color.CYAN
                not.enableLights(true)
                not.enableVibration(true)
                not
            }
            ACTION_SYNC_DEVICE -> {
                val channelInfo = Pair(CHANNEL_ID_SYNC_DEVICE, ctx.getString(R.string.sync_device_notification))
                val not = NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
                not.lightColor = Color.CYAN
                not.enableLights(true)
                not.enableVibration(true)
                not
            }
            ACTION_JOB_BACKUP -> {
                val channelInfo = Pair(CHANNEL_ID_JOB_BACKUP, ctx.getString(R.string.job_backup_notification))
                val not = NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_LOW)
                not
            }
            else -> {
                //Error Channel
                val channelInfo = Pair(CHANNEL_ID_ERROR, ctx.getString(R.string.error_notification))
                NotificationChannel(channelInfo.first, channelInfo.second, NotificationManager.IMPORTANCE_HIGH)
            }
        }
    }
}