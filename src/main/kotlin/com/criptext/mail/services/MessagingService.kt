package com.criptext.mail.services

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.Notifier
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.github.kittinunf.result.Result
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MessagingService : FirebaseMessagingService(){

    private var pushController: PushController? = null


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(pushController == null){
            val db = Result.of { AppDatabase.getAppDatabase(this) }
            if(db is Result.Success) {
                val storage = KeyValueStorage.SharedPrefs(this)
                val account = ActiveAccount.loadFromStorage(this) ?: return
                pushController = PushController(
                        dataSource = PushDataSource(db = db.value,
                                runner = AsyncTaskWorkRunner(),
                                httpClient = HttpClient.Default(),
                                activeAccount = account,
                                storage = storage,
                                filesDir = this.filesDir,
                                cacheDir = this.cacheDir),
                        host = this,
                        isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                        activeAccount = account,
                        storage = storage)
            }
        }
        if(remoteMessage.data.isNotEmpty()) {
            val shouldPostNotification = !isAppOnForeground(this, packageName)
            pushController?.parsePushPayload(remoteMessage.data, shouldPostNotification)
        }
    }

    fun notifyPushEvent(notifier: Notifier?){
        notifier?.notifyPushEvent(this)
    }

    fun cancelPush(notificationId: Int, storage: KeyValueStorage){
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        manager.cancel(notificationId)
        if((notCount - 1) == 0) {
            manager.cancel(CriptextNotification.INBOX_ID)
        }
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, notCount - 1)
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