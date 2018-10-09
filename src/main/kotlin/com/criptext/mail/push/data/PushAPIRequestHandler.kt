package com.criptext.mail.push.data

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.HttpClient
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.push.PushTypes
import com.github.kittinunf.result.Result
import org.json.JSONObject

class PushAPIRequestHandler(private val ctx: Context, private val manager: NotificationManager){
    private val activeAccount = ActiveAccount.loadFromStorage(ctx)!!
    private val apiClient = PushAPIClient(HttpClient.Default(), activeAccount.jwt)

    private val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    fun linkAccept(deviceId: String, notificationId: Int): Int {
        val operation = Result.of {
            JSONObject(apiClient.postLinkAccept(deviceId)).getInt("deviceId")
        }
        return when(operation){
            is Result.Success -> {
                manager.cancel(notificationId)
                operation.value
            }
            is Result.Failure -> {
                manager.cancel(notificationId)
                val data = ErrorNotificationData(ctx.getString(R.string.push_link_error_title),
                        ctx.getString(R.string.push_link_error_message_approve))
                val not = CriptextNotification(ctx)
                val errorNot = not.createErrorNotification(data.title, data.body)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
                -1
            }
        }
    }

    fun linkDeny(deviceId: String, notificationId: Int){
        val operation = Result.of { apiClient.postLinkDeny(deviceId) }
        when(operation){
            is Result.Success -> manager.cancel(notificationId)
            is Result.Failure -> {
                manager.cancel(notificationId)
                val data = ErrorNotificationData(ctx.getString(R.string.push_link_error_title),
                        ctx.getString(R.string.push_link_error_message_deny))
                val not = CriptextNotification(ctx)
                val errorNot = not.createErrorNotification(data.title, data.body)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
            }
        }
    }

    private fun postNotification(data: ErrorNotificationData, cn: CriptextNotification,
                                 notification: Notification) {
        cn.notify(if(isPostNougat) PushTypes.linkDevice.requestCodeRandom() else
            PushTypes.linkDevice.requestCode(), notification,
                CriptextNotification.ACTION_LINK_DEVICE)
    }

    private fun postHeaderNotification(cn: CriptextNotification, data: ErrorNotificationData){
        cn.showHeaderNotification(data.title, R.drawable.push_icon,
                CriptextNotification.ACTION_ERROR)
    }

    private fun notifyPushEvent(data: ErrorNotificationData, cn: CriptextNotification,
                                notification: Notification) {
        if(isPostNougat) {
            postHeaderNotification(cn, data)
        }
        postNotification(data, cn, notification)
    }

    private data class ErrorNotificationData(val title: String, val body: String)
}