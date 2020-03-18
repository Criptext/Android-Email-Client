package com.criptext.mail.services

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.crashlytics.android.Crashlytics
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.notifiers.Notifier
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.github.kittinunf.result.Result
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.fabric.sdk.android.Fabric


class MessagingService : FirebaseMessagingService(){

    private var pushController: PushController? = null


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if(pushController == null){
            val db = Result.of {
                Fabric.with(this, Crashlytics())
                AppDatabase.getAppDatabase(this)
            }
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

    fun cancelPush(notificationId: Int, storage: KeyValueStorage, key: KeyValueStorage.StringKey, headerId: Int){
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notCount = storage.getInt(key, 0)
        manager.cancel(notificationId)
        if((notCount - 1) <= 0) {
            manager.cancel(headerId)
        }
        storage.putInt(key, if(notCount <= 0) 0 else notCount - 1)
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