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
import com.criptext.mail.db.models.Contact
import com.criptext.mail.push.data.PushAPIRequestHandler


class NewMailActionService : IntentService("New Mail Action Service") {

    companion object {
        const val READ = "Read"
        const val TRASH = "Trash"
        const val REPLY = "Reply"
        const val DELETE = "Delete"
    }



    public override fun onHandleIntent(intent: Intent?) {
        val storage = KeyValueStorage.SharedPrefs(this)
        var activeAccount = ActiveAccount.loadFromStorage(storage)!!
        val data = getIntentData(intent, activeAccount.recipientId)
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val db = AppDatabase.getAppDatabase(this)
        if(activeAccount.userEmail != data.recipientId.plus("@${data.domain}"))
            activeAccount = ActiveAccount.loadFromDB(db.accountDao().getAccount(data.recipientId, data.domain)!!)!!
        val requestHandler = PushAPIRequestHandler(NotificationError(this), manager,
                activeAccount, HttpClient.Default(),
                storage)


        when (data.action){
            READ -> {
                requestHandler.openEmail(data.metadataKey, data.notificationId, db.emailDao(), db.pendingEventDao(), db.accountDao())
            }
            TRASH -> {
                requestHandler.trashEmail(data.metadataKey, data.notificationId,
                        EmailDetailLocalDB.Default(db, this.filesDir), db.emailDao(), db.pendingEventDao(), db.accountDao())
            }
            DELETE -> {
                val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
                if((notCount - 1) <= 0) {
                    manager.cancel(CriptextNotification.INBOX_ID)
                }
                storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount,
                        if(notCount <= 0) 0 else notCount - 1)
            }
            else -> throw IllegalArgumentException("Unsupported action: " + data.action)
        }
    }

    private fun getIntentData(intent: Intent?, activeRecipientId: String): IntentData {
        val action = intent!!.action ?: ""
        val notificationId = intent.getIntExtra("notificationId", 0)
        val metadataKey = intent.getLongExtra("metadataKey", 0)
        val recipientId = intent.getStringExtra("account") ?: activeRecipientId
        val domain = intent.getStringExtra("domain") ?: Contact.mainDomain
        return IntentData(action, metadataKey, notificationId, recipientId, domain)
    }

    private data class IntentData(val action: String, val metadataKey: Long, val notificationId: Int,
                                  val recipientId: String, val domain: String)
}