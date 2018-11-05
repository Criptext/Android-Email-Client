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
    }



    public override fun onHandleIntent(intent: Intent?) {
        val data = getIntentData(intent)
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val requestHandler = PushAPIRequestHandler(NotificationError(this), manager,
                ActiveAccount.loadFromStorage(this)!!, HttpClient.Default(),
                KeyValueStorage.SharedPrefs(this))
        val db = AppDatabase.getAppDatabase(this)

        when {
            READ == data.action -> {
                requestHandler.openEmail(data.metadataKey, data.notificationId, db.emailDao(), db.pendingEventDao())
            }
            TRASH == data.action -> {
                requestHandler.trashEmail(data.metadataKey, data.notificationId,
                        EmailDetailLocalDB.Default(db), db.emailDao(), db.pendingEventDao())
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