package com.criptext.mail.push

import android.app.Notification
import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager
import android.os.Vibrator
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationNewMail

/**
 * Created by gabriel on 8/21/17.
 */

sealed class NewMailNotifier(val data: PushData.NewMail): Notifier {
    companion object {
        private val type = PushTypes.newMail
    }


    private fun postNotification(ctx: Context, isPostNougat: Boolean) {
        val cn = NotificationNewMail(ctx)
        val notification = buildNotification(ctx, cn)
        cn.notify(notification.first, notification.second, CriptextNotification.ACTION_INBOX)
    }

    private fun postHeaderNotification(ctx: Context){
        val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, PushTypes.openActivity,
                data.threadId, data.isPostNougat)
        val cn = NotificationNewMail(ctx)
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_INBOX, pendingIntent)
    }

    private fun postForegroundrNotification(ctx: Context){
        val am = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager?

        when (am!!.ringerMode) {
            AudioManager.RINGER_MODE_VIBRATE -> setNotification(false, ctx)
            AudioManager.RINGER_MODE_NORMAL -> setNotification(true, ctx)
        }
    }

    private fun setNotification(sound: Boolean, ctx: Context){
        val vibrate: Vibrator = ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate.vibrate(400)
        if(sound){
            val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(ctx.applicationContext, defaultSound)
            ringtone.play()
        }
    }

    protected abstract fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification>

    override fun notifyPushEvent(ctx: Context) {
        if (data.shouldPostNotification){
            if(data.isPostNougat) {
                postHeaderNotification(ctx)
            }
            postNotification(ctx, data.isPostNougat)
        }else{
            postForegroundrNotification(ctx)
        }
    }

    class Single(data: PushData.NewMail): NewMailNotifier(data) {

        override fun buildNotification(ctx: Context, cn: CriptextNotification): Pair<Int, Notification> {
            val pendingIntent = ActivityIntentFactory.buildSceneActivityPendingIntent(ctx, type,
                data.threadId, data.isPostNougat)

            val notificationId = if(data.isPostNougat) type.requestCodeRandom() else type.requestCode()

            return Pair(notificationId, cn.createNotification(clickIntent = pendingIntent, data = data,
                    notificationId = notificationId))

        }
    }

}