package com.criptext.mail.push.services

import android.app.IntentService
import android.content.Intent
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Contact


class HeaderActionService : IntentService("Header Action Service") {

    public override fun onHandleIntent(intent: Intent?) {
        val storage = KeyValueStorage.SharedPrefs(this)
        val activeAccount = ActiveAccount.loadFromStorage(storage)!!
        val data = getIntentData(intent, activeAccount.recipientId)


        when (data.action){
            CriptextNotification.ACTION_INBOX -> storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
            CriptextNotification.ACTION_LINK_DEVICE,
            CriptextNotification.ACTION_SYNC_DEVICE -> storage.putInt(KeyValueStorage.StringKey.SyncNotificationCount, 0)
            CriptextNotification.ACTION_JOB_BACKUP -> storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
            else -> {}
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