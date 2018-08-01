package com.criptext.mail.services

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.data.PushRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService(){

    private val pushController = PushController(
            dataSource = PushDataSource(db = AppDatabase.getAppDatabase(this),
                                        runner = AsyncTaskWorkRunner(),
                                        httpClient = HttpClient.Default()),
            isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(remoteMessage.data.isNotEmpty()) {
            val shouldPostNotification = !isAppOnForeground(this, packageName)
            val notifier = pushController.parsePushPayload(remoteMessage.data, shouldPostNotification)
            notifier?.notifyPushEvent(this)
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