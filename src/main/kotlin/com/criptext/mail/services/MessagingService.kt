package com.criptext.mail.services

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.HttpClient
import com.criptext.mail.bgworker.ProgressReporter
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EventLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.PushController
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.push.notifiers.*
import com.criptext.mail.push.workers.GetPushEmailWorker
import com.criptext.mail.push.workers.RemoveNotificationWorker
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.eventhelper.EventHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.io.File


class MessagingService : FirebaseMessagingService(){

    private var pushController: PushController? = null
    private val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val shouldPostNotification = !isAppOnForeground(this.applicationContext, this.applicationContext.packageName)
        if(remoteMessage.data.isNotEmpty()
                && shouldPostNotification) {
            val getPushWorker = newPushWorker(remoteMessage.data, filesDir, AppDatabase.getAppDatabase(this), shouldPostNotification)
            val result = getPushWorker.work(reporter)
            when(result){
                is PushResult.NewEmail.Success -> {
                    createAndNotifyPush(result.pushData, result.shouldPostNotification,
                            result.senderImage, result.notificationId, result.activeAccount, true)
                }
                is PushResult.NewEmail.Failure -> {
                    if(result.exception !is EventHelper.NoContentFoundException
                            || result.exception !is EventHelper.AccountNotFoundException)
                        createAndNotifyPush(result.pushData, result.shouldPostNotification,
                                null, result.notificationId, result.activeAccount!!,false)

                }
            }
            val intent = Intent(applicationContext, DecryptionService::class.java)
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

    private fun createAndNotifyPush(pushData: Map<String, String>, shouldPostNotification: Boolean,
                                    senderImage: Bitmap?, notificationId: Int, activeAccount: ActiveAccount,
                                    isSuccess: Boolean){
        val action = pushData["action"]
        if (action != null) {
            val notifier =  when (PushTypes.fromActionString(action)) {
                PushTypes.newMail -> {
                    if(isSuccess) {
                        val data = PushData.NewMail.parseNewMailPush(pushData, shouldPostNotification, senderImage, isPostNougat, activeAccount.userEmail)
                        NewMailNotifier.Single(data, notificationId)
                    } else {
                        val data = PushData.Error(UIMessage(R.string.push_email_update_mailbox_title),
                                UIMessage(R.string.push_email_update_mailbox_body),
                                isPostNougat, shouldPostNotification)
                        ErrorNotifier.Open(data)
                    }
                }
                PushTypes.linkDevice -> {
                    val data = PushData.LinkDevice.parseLinkDevicePush(pushData, shouldPostNotification, isPostNougat)
                    LinkDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.openActivity -> {
                    val data = PushData.OpenMailbox.parseNewOpenMailbox(pushData, shouldPostNotification, isPostNougat)
                    OpenMailboxNotifier.Open(data)
                }
                PushTypes.syncDevice -> {
                    val data = PushData.SyncDevice.parseSyncDevicePush(pushData, shouldPostNotification, isPostNougat)
                    SyncDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.antiPush -> {
                    when(pushData["subAction"]){
                        "delete_new_email" -> {
                            val metadataKeys = pushData["metadataKeys"]?.split(",")
                            metadataKeys?.forEach {
                                val removeNotificationWorker = newRemoveNotificationWorker(pushData, AppDatabase.getAppDatabase(this), it)
                                val result = removeNotificationWorker.work(reporter)
                                onRemoveNotification(result)
                            }
                        }
                        "delete_sync_link" -> {
                            val randomId = pushData["randomId"] ?: ""
                            if(randomId.isNotEmpty()) {
                                val removeNotificationWorker = newRemoveNotificationWorker(pushData, AppDatabase.getAppDatabase(this), randomId)
                                val result = removeNotificationWorker.work(reporter)
                                onRemoveNotification(result)
                            }
                        }
                    }
                    null
                }
                else -> null
            }
            notifier?.notifyPushEvent(applicationContext)
        }
    }

    private fun onRemoveNotification(result: PushResult.RemoveNotification?){
        when(result){
            is PushResult.RemoveNotification.Success -> {
                when(result.antiPushSubtype) {
                    "delete_new_email" -> {
                        cancelPush(result.notificationId,
                                KeyValueStorage.StringKey.NewMailNotificationCount, CriptextNotification.INBOX_ID)
                    }
                    "delete_sync_link" -> {
                        cancelPush(result.notificationId,
                                KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
                    }
                }
            }
        }
    }

    private fun cancelPush(notificationId: Int, key: KeyValueStorage.StringKey, headerId: Int){
        val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val storage = KeyValueStorage.SharedPrefs(applicationContext)
        val notCount = storage.getInt(key, 0)
        manager.cancel(notificationId)
        if((notCount - 1) <= 0) {
            manager.cancel(headerId)
        }
        storage.putInt(key, if(notCount <= 0) 0 else notCount - 1)
    }

    private fun newPushWorker(pushData: Map<String, String>, filesDir: File, db: AppDatabase, shouldPostNotification: Boolean): GetPushEmailWorker =
            GetPushEmailWorker(db = db, publishFn = {}, httpClient = HttpClient.Default(),
                    shouldPostNotification = shouldPostNotification, dbEvents = EventLocalDB(db, filesDir, cacheDir),
                    label = Label.defaultItems.inbox, pushData = pushData)
    private fun newRemoveNotificationWorker(pushData: Map<String, String>, db: AppDatabase, value: String): RemoveNotificationWorker =
            RemoveNotificationWorker(db = db, publishFn = {}, pushData = pushData,
                    notificationValue = value)

    private val reporter = object: ProgressReporter<PushResult> {
        override fun report(progressPercentage: PushResult) {

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