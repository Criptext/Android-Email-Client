package com.criptext.mail.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.crashlytics.android.Crashlytics
import com.criptext.mail.R
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.notifiers.Notifier
import io.fabric.sdk.android.Fabric

class DecryptionForegroundService(private val appContext: Context, params: WorkerParameters) :
        CoroutineWorker(appContext, params) {
    private var pushController: PushController? = null
    private val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result {
        if(pushController == null){
            val db = com.github.kittinunf.result.Result.of {
                Fabric.with(appContext, Crashlytics())
                AppDatabase.getAppDatabase(appContext)
            }
            if(db is com.github.kittinunf.result.Result.Success) {
                val storage = KeyValueStorage.SharedPrefs(appContext)
                val account = ActiveAccount.loadFromStorage(appContext) ?: return Result.failure()
//                pushController = PushController(
//                        dataSource = PushDataSource(db = db.value,
//                                runner = AsyncTaskWorkRunner(),
//                                httpClient = HttpClient.Default(),
//                                activeAccount = account,
//                                storage = storage,
//                                filesDir = appContext.filesDir,
//                                cacheDir = appContext.cacheDir),
//                        host = this,
//                        isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
//                        activeAccount = account,
//                        storage = storage)
            }
        }
        val pushData = inputData.keyValueMap as Map<String, String>
        if(pushData.isNotEmpty()) {
            prepareNotification()
            val shouldPostNotification = !MessagingService.isAppOnForeground(appContext, appContext.packageName)
            pushController?.parsePushPayload(pushData, shouldPostNotification)
        } else {
            pushController = null
        }
        while (pushController != null){
            if(MessagingService.isAppOnForeground(appContext, appContext.packageName)) {
                pushController = null
            }
        }
        return Result.success()
    }

    private suspend fun prepareNotification(){
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Decrypting Emails")
                .build()

        val foregroundInfo = ForegroundInfo(NOTIFICATION_ID, notification)
        setForeground(foregroundInfo)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                    notificationManager.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                        channelId, TAG, NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun endService(){
        pushController = null
    }

    fun notifyPushEvent(notifier: Notifier?){
        notifier?.notifyPushEvent(appContext)
    }

    fun cancelPush(notificationId: Int, storage: KeyValueStorage, key: KeyValueStorage.StringKey, headerId: Int){
        val manager = appContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notCount = storage.getInt(key, 0)
        manager.cancel(notificationId)
        if((notCount - 1) <= 0) {
            manager.cancel(headerId)
        }
        storage.putInt(key, if(notCount <= 0) 0 else notCount - 1)
    }

    companion object {

        // From SystemForegroundDispatcher
        const val NOTIFICATION_ID = 42
        const val TAG = "ForegroundWorker"
        const val channelId = "Job progress"
        const val Progress = "Progress"
        private const val delayDuration = 100L
    }

}