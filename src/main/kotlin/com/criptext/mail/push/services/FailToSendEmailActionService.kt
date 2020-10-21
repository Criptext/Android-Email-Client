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
import com.criptext.mail.push.ActivityIntentFactory
import com.criptext.mail.push.data.PushAPIRequestHandler
import com.criptext.mail.scenes.mailbox.MailboxActivity
import com.criptext.mail.services.MessagingInstance


class FailToSendEmailActionService : IntentService("Fail To Send Action Service") {

    companion object {
        const val RETRY = "Retry"
        const val DISMISS = "Dismiss"
    }



    public override fun onHandleIntent(intent: Intent?) {
        val storage = KeyValueStorage.SharedPrefs(this)
        val data = getIntentData(intent)
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        when (intent!!.action){
            RETRY -> {
                val mailboxIntent = Intent(baseContext, MailboxActivity::class.java)
                mailboxIntent.action = Intent.ACTION_MAIN
                mailboxIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                mailboxIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                deletePush(storage, manager)
                application.startActivity(mailboxIntent)
            }
            DISMISS -> {
                deletePush(storage, manager)
            }
            else -> throw IllegalArgumentException("Unsupported action: " + data.action)
        }
    }

    private fun deletePush(storage: KeyValueStorage, manager: NotificationManager){
        val notCount = storage.getInt(KeyValueStorage.StringKey.FailToSendNotificationCount, 0)
        if((notCount - 1) <= 0) {
            manager.cancel(CriptextNotification.FAIL_TO_SEND_ID)
        }
        storage.putInt(KeyValueStorage.StringKey.FailToSendNotificationCount,
                if(notCount <= 0) 0 else notCount - 1)
    }

    private fun getIntentData(intent: Intent?): IntentData {
        val action = intent!!.action ?: ""
        return IntentData(action)
    }

    private data class IntentData(val action: String)
}