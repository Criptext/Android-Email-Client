package com.criptext.mail.push.services

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.androidui.criptextnotification.NotificationError
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.data.PushAPIRequestHandler


class LinkDeviceActionService : IntentService("Link Device Action Service") {

    companion object {
        const val APPROVE = "Approve Link"
        const val DENY = "Deny Link"
    }



    public override fun onHandleIntent(intent: Intent?) {
        val storage = KeyValueStorage.SharedPrefs(this)
        var activeAccount = ActiveAccount.loadFromStorage(storage)!!
        val data = getIntentData(intent, activeAccount.recipientId)
        val db = AppDatabase.getAppDatabase(this)
        if(activeAccount.recipientId != data.recipientId)
            activeAccount = ActiveAccount.loadFromDB(db.accountDao().getAccountByRecipientId(data.recipientId)!!)!!
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val requestHandler = PushAPIRequestHandler(NotificationError(this), manager,
                activeAccount, HttpClient.Default(),
                KeyValueStorage.SharedPrefs(this))

        when {
            APPROVE == data.action -> {
                requestHandler.linkAccept(data.randomId, data.notificationId)
            }
            DENY == data.action -> {
                requestHandler.linkDeny(data.randomId, data.notificationId)
            }
            else -> throw IllegalArgumentException("Unsupported action: " + data.action)
        }
    }

    private fun getIntentData(intent: Intent?, activeRecipientId: String): IntentData {
        val action = intent!!.action ?: ""
        val notificationId = intent.getIntExtra("notificationId", 0)
        val randomId = intent.getStringExtra("randomId")
        val recipientId = intent.getStringExtra("account") ?: activeRecipientId
        return IntentData(action, randomId, notificationId, recipientId)
    }

    private data class IntentData(val action: String, val randomId: String, val notificationId: Int, val recipientId: String)
}