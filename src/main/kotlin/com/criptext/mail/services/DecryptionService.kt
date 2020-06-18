package com.criptext.mail.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.crashlytics.android.Crashlytics
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.getLocalizedUIMessage
import io.fabric.sdk.android.Fabric


class DecryptionService: Service() {

    private var pushController: PushController? = null
    private var notificationManager: NotificationManager? = null
    private var queue: MutableList<() -> Unit?> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(CriptextNotification.ACTION_FOREGROUND_DECRYPT, "ON START COMMAND")

        if (intent != null) {

            when (intent.action) {
                ACTION_START_SERVICE -> {
                    Log.d(CriptextNotification.ACTION_FOREGROUND_DECRYPT, ACTION_START_SERVICE)
                    notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    startForegroundService()
                    getEvents()
                }
                ACTION_ADD_NOTIFICATION_TO_QUEUE -> {
                    Log.d(CriptextNotification.ACTION_FOREGROUND_DECRYPT, ACTION_ADD_NOTIFICATION_TO_QUEUE)
                    queue.add { getEvents() }
                }
                else -> {
                    Log.d(CriptextNotification.ACTION_FOREGROUND_DECRYPT, "START_NOT_STICKY: ${intent.action}")
                    stopService()
                    return START_NOT_STICKY
                }
            }

        } else {
            stopService()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startForegroundService() {

        //Create Notification channel for all the notifications sent from this app.
        createNotificationChannel()

        // Start foreground service.
        prepareNotification()

    }

    private fun getEvents(){
        if(pushController == null){
            val db = com.github.kittinunf.result.Result.of {
                Fabric.with(applicationContext, Crashlytics())
                AppDatabase.getAppDatabase(applicationContext)
            }
            if(db is com.github.kittinunf.result.Result.Success) {
                val storage = KeyValueStorage.SharedPrefs(applicationContext)
                val account = ActiveAccount.loadFromStorage(applicationContext) ?: return
                pushController = PushController(
                        dataSource = PushDataSource(db = db.value,
                                runner = AsyncTaskWorkRunner(),
                                httpClient = HttpClient.Default(),
                                activeAccount = account,
                                storage = storage,
                                filesDir = applicationContext.filesDir,
                                cacheDir = applicationContext.cacheDir),
                        host = this,
                        storage = storage)
            }
        }
        pushController?.doGetEvents()
    }

    private fun stopService() {
        Log.d(CriptextNotification.ACTION_FOREGROUND_DECRYPT, ACTION_STOP_FOREGROUND_SERVICE)
        // Stop foreground service and remove the notification.
        stopForeground(true)
        // Stop the foreground service.
        stopSelf()

        IS_RUNNING = false
    }


    override fun onDestroy() {
        IS_RUNNING = false
    }

    private fun prepareNotification(){
        val notification = NotificationCompat.Builder(applicationContext, CriptextNotification.CHANNEL_ID_DECRYPTION_SERVICE)
                .setSmallIcon(R.drawable.push_icon)
                .setContentTitle(this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_notification)))
                .setContentText(this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_message)))
                .setOngoing(true)
                .setGroup(CriptextNotification.ACTION_FOREGROUND_DECRYPT)
                .setGroupSummary(false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_message))))
                .setProgress(0,0,true)
                .build()
        startForeground(CriptextNotification.DECRYPTION_SERVICE_ID, notification)
        IS_RUNNING = true
    }

    fun updateServiceProgress(progress: Int, max: Int){
        val notification = NotificationCompat.Builder(applicationContext, CriptextNotification.CHANNEL_ID_DECRYPTION_SERVICE)
                .setSmallIcon(R.drawable.push_icon)
                .setContentTitle(this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_notification)))
                .setContentText(this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_message_progress,
                        arrayOf(progress, max))))
                .setOngoing(true)
                .setGroup(CriptextNotification.ACTION_FOREGROUND_DECRYPT)
                .setGroupSummary(false)
                .setStyle(NotificationCompat.BigTextStyle().bigText(this.getLocalizedUIMessage(
                        UIMessage(R.string.foreground_decryption_message_progress,
                                arrayOf(progress, max)
                        )
                )))
                .setProgress(max, progress,false)
                .build()
        notificationManager?.notify(CriptextNotification.DECRYPTION_SERVICE_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                    notificationManager?.getNotificationChannel(CriptextNotification.CHANNEL_ID_DECRYPTION_SERVICE)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                        CriptextNotification.CHANNEL_ID_DECRYPTION_SERVICE,
                        this.getLocalizedUIMessage(UIMessage(R.string.foreground_decryption_notification)),
                        NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun checkQueuedEvents(){
        if(queue.isNotEmpty()) {
            queue.first().let { it() }
            queue.removeAt(0)
        } else {
            endService()
        }
    }

    private fun endService(){
        pushController = null
        queue.clear()
        stopService()
    }

    fun cancelPush(notificationId: Int, storage: KeyValueStorage, key: KeyValueStorage.StringKey, headerId: Int){
        val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notCount = storage.getInt(key, 0)
        manager.cancel(notificationId)
        if((notCount - 1) <= 0) {
            manager.cancel(headerId)
        }
        storage.putInt(key, if(notCount <= 0) 0 else notCount - 1)
    }

    companion object {


        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        const val ACTION_OPEN_APP = "ACTION_OPEN_APP"
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_ADD_NOTIFICATION_TO_QUEUE = "ACTION_ADD_NOTIFICATION_TO_QUEUE"

        var IS_RUNNING: Boolean = false
    }

}