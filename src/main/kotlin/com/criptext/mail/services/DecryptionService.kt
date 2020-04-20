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
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.AsyncTaskWorkRunner
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushController
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.notifiers.Notifier
import io.fabric.sdk.android.Fabric

class DecryptionService: Service() {

    private var pushController: PushController? = null
    private var notificationManager: NotificationManager? = null
    private var data: HashMap<String, String>? = null
    private var queue: MutableList<HashMap<String, String>?> = mutableListOf()

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        Log.d(TAG, "ON START COMMAND")

        if (intent != null) {

            when (intent.action) {
                ACTION_START_SERVICE -> {
                    data = intent.extras?.getSerializable("data") as HashMap<String, String>
                    getPushNotification()
                }
                ACTION_STOP_FOREGROUND_SERVICE -> stopService()
                ACTION_OPEN_APP -> stopService()
                ACTION_ADD_NOTIFICATION_TO_QUEUE -> {
                    queue.add(intent.extras?.getSerializable("data") as HashMap<String, String>)
                }
            }

        }
        return START_STICKY
    }

    private fun startForegroundService() {

        //Create Notification channel for all the notifications sent from this app.
        createNotificationChannel()

        // Start foreground service.
        prepareNotification()

    }

    private fun getPushNotification(){
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
                        isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N,
                        activeAccount = account,
                        storage = storage)
            }
        }
        val pushData = data as Map<String, String>
        if(pushData.isNotEmpty()) {
            val shouldPostNotification = !MessagingService.isAppOnForeground(applicationContext, applicationContext.packageName)
            pushController?.parsePushPayload(pushData, shouldPostNotification)
        } else {
            pushController = null
        }
    }

    private fun stopService() {
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
        createNotificationChannel()
        val notification = NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Decrypting Emails")
                .build()
        startForeground(NOTIFICATION_ID, notification)
        IS_RUNNING = true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            var notificationChannel =
                    notificationManager?.getNotificationChannel(channelId)
            if (notificationChannel == null) {
                notificationChannel = NotificationChannel(
                        channelId, TAG, NotificationManager.IMPORTANCE_LOW
                )
                notificationManager?.createNotificationChannel(notificationChannel)
            }
        }
    }

    fun pushProcessed(){
        if(queue.isEmpty()) {
            pushController?.doGetEvents()
        } else {
            data = queue.first()
            queue.removeAt(0)
            getPushNotification()
        }
    }

    fun endService(){
        pushController = null
        stopService()
    }

    fun notifyPushEvent(notifier: Notifier?){
        notifier?.notifyPushEvent(applicationContext)
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
        const val NOTIFICATION_ID = 42
        const val TAG = "ForegroundWorker"
        const val channelId = "Job progress"
        const val Progress = "Progress"


        const val ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE"

        const val ACTION_OPEN_APP = "ACTION_OPEN_APP"
        const val ACTION_START_SERVICE = "ACTION_START_SERVICE"
        const val ACTION_ADD_NOTIFICATION_TO_QUEUE = "ACTION_ADD_NOTIFICATION_TO_QUEUE"

        var IS_RUNNING: Boolean = false
    }

}