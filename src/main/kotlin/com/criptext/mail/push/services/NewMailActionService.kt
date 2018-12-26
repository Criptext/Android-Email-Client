package com.criptext.mail.push.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationError
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.data.PushAPIRequestHandler


class NewMailActionService : IntentService("New Mail Action Service") {

    companion object {
        const val READ = "Read"
        const val TRASH = "Trash"
        const val REPLY = "Reply"
        const val DELETE = "Delete"
    }



    public override fun onHandleIntent(intent: Intent?) {
        val data = getIntentData(intent)
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val storage = KeyValueStorage.SharedPrefs(this)
        val requestHandler = PushAPIRequestHandler(NotificationError(this), manager,
                ActiveAccount.loadFromStorage(this)!!, HttpClient.Default(),
                storage)
        val db = AppDatabase.getAppDatabase(this)

        when (data.action){
            READ -> {
                requestHandler.openEmail(data.metadataKey, data.notificationId, db.emailDao(), db.pendingEventDao(), db.accountDao())
            }
            TRASH -> {
                requestHandler.trashEmail(data.metadataKey, data.notificationId,
                        EmailDetailLocalDB.Default(db), db.emailDao(), db.pendingEventDao(), db.accountDao())
            }
            DELETE -> {
                val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
                if((notCount - 1) == 0) {
                    manager.cancel(CriptextNotification.INBOX_ID)
                }
                val newNotCount = if(notCount == 0) notCount else notCount - 1
                storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, newNotCount)
            }
            else -> throw IllegalArgumentException("Unsupported action: " + data.action)
        }
    }

    private fun getIntentData(intent: Intent?): IntentData {
        val action = intent!!.action
        val notificationId = intent.getIntExtra("notificationId", 0)
        val metadataKey = intent.getLongExtra("metadataKey", 0)
        return IntentData(action, metadataKey, notificationId)
    }

    private data class IntentData(val action: String, val metadataKey: Long, val notificationId: Int)
}