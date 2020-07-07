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
        val data = getIntentData(intent)


        when (data.action){
            CriptextNotification.ACTION_INBOX -> storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
            CriptextNotification.ACTION_LINK_DEVICE,
            CriptextNotification.ACTION_SYNC_DEVICE -> storage.putInt(KeyValueStorage.StringKey.SyncNotificationCount, 0)
            CriptextNotification.ACTION_JOB_BACKUP -> storage.putInt(KeyValueStorage.StringKey.CloudBackupNotificationCount, 0)
            else -> {}
        }
    }

    private fun getIntentData(intent: Intent?): IntentData {
        val action = intent!!.action ?: ""
        return IntentData(action)
    }

    private data class IntentData(val action: String)
}