package com.criptext.mail.push.data

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent


class LinkDeviceActionService : IntentService("Link Device Action Service") {

    companion object {
        const val APPROVE = "Approve"
        const val DENY = "Deny"
    }



    public override fun onHandleIntent(intent: Intent?) {
        val data = getIntentData(intent)
        val manager = this.applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val requestHandler = PushAPIRequestHandler(this, manager)

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

    private fun getIntentData(intent: Intent?): IntentData{
        val action = intent!!.action
        val notificationId = intent.getIntExtra("notificationId", 0)
        val randomId = intent.getStringExtra("randomId")
        return IntentData(action, randomId, notificationId)
    }

    private data class IntentData(val action: String, val randomId: String, val notificationId: Int)
}