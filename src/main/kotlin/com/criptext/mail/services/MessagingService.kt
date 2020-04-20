package com.criptext.mail.services

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.push.PushController
import com.criptext.mail.push.notifiers.Notifier
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.Serializable


class MessagingService : FirebaseMessagingService(){


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.data.isNotEmpty()) {
            val intent = Intent(applicationContext, DecryptionService::class.java)
            val hashMap = HashMap<String, String>()
            hashMap.putAll(remoteMessage.data)
            intent.putExtra("data", hashMap)
            if (!DecryptionService.IS_RUNNING) {
                intent.action = DecryptionService.ACTION_START_SERVICE
            } else {
                intent.action = DecryptionService.ACTION_ADD_NOTIFICATION_TO_QUEUE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        }
    }

    companion object {

        fun isAppOnForeground(context: Context, appPackageName: String): Boolean {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses ?: return false
            return appProcesses.any {
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && it.processName == appPackageName
            }
        }
    }
}