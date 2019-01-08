package com.criptext.mail.push.data

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.HttpClient
import com.criptext.mail.api.HttpErrorHandlingHelper
import com.criptext.mail.api.PeerAPIClient
import com.criptext.mail.api.PeerEventsApiHandler
import com.criptext.mail.db.AppDatabase
import com.criptext.mail.db.EmailDetailLocalDB
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.dao.AccountDao
import com.criptext.mail.db.dao.EmailDao
import com.criptext.mail.db.dao.PendingEventDao
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.EmailLabel
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.PushData
import com.criptext.mail.push.PushTypes
import com.criptext.mail.scenes.emaildetail.data.EmailDetailResult
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.PeerQueue
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerChangeEmailLabelData
import com.criptext.mail.utils.peerdata.PeerOpenEmailData
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import com.github.kittinunf.result.mapError
import org.json.JSONObject

class PushAPIRequestHandler(private val not: CriptextNotification,
                            private val manager: NotificationManager,
                            val activeAccount: ActiveAccount,
                            val httpClient: HttpClient,
                            private val storage: KeyValueStorage){

    private val apiClient = PushAPIClient(httpClient, activeAccount.jwt)

    private val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    fun linkAccept(deviceId: String, notificationId: Int): Int {
        val operation = Result.of {
            JSONObject(apiClient.postLinkAccept(deviceId).body).getInt("deviceId")
        }
        return when(operation){
            is Result.Success -> {
                manager.cancel(notificationId)
                operation.value
            }
            is Result.Failure -> {
                manager.cancel(notificationId)
                val data = PushData.Error(UIMessage(R.string.push_link_error_title),
                        UIMessage(R.string.push_link_error_message_approve), isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
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
                val data = PushData.Error(UIMessage(R.string.push_link_error_title),
                        UIMessage(R.string.push_link_error_message_deny), isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
            }
        }
    }

    fun syncAccept(deviceId: String, notificationId: Int): Int {
        val operation = Result.of {
            JSONObject(apiClient.postSyncAccept(deviceId).body).getInt("deviceId")
        }
        return when(operation){
            is Result.Success -> {
                manager.cancel(notificationId)
                operation.value
            }
            is Result.Failure -> {
                manager.cancel(notificationId)
                val data = PushData.Error(UIMessage(R.string.push_link_error_title),
                        UIMessage(R.string.push_link_error_message_approve), isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
                -1
            }
        }
    }

    fun syncDeny(deviceId: String, notificationId: Int){
        val operation = Result.of { apiClient.postSyncDeny(deviceId) }
        when(operation){
            is Result.Success -> manager.cancel(notificationId)
            is Result.Failure -> {
                manager.cancel(notificationId)
                val data = PushData.Error(UIMessage(R.string.push_link_error_title),
                        UIMessage(R.string.push_link_error_message_deny), isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
            }
        }
    }

    fun openEmail(metadataKey: Long, notificationId: Int, emailDao: EmailDao, pendingDao: PendingEventDao, accountDao: AccountDao){
        handleNotificationCountForNewEmail(notificationId)
        val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao, storage, accountDao)
        val operation = Result.of {
            val email = emailDao.getEmailByMetadataKey(metadataKey)
            email
        }
        .flatMap { Result.of { emailDao.toggleCheckingRead(listOf(it.id), false) } }



        peerEventsApiHandler.enqueueEvent(PeerOpenEmailData(listOf(metadataKey)).toJSON())
        peerEventsApiHandler.enqueueEvent(PeerReadEmailData(listOf(metadataKey), false).toJSON())

        when(operation){
            is Result.Success -> {
            }
            is Result.Failure -> {
                operation.error.printStackTrace()
                val data = PushData.Error(UIMessage(R.string.push_email_error_title),
                        UIMessage(R.string.push_mail_error_message_read), isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
            }
        }
    }

    fun trashEmail(metadataKey: Long, notificationId: Int, db: EmailDetailLocalDB, emailDao: EmailDao, pendingDao: PendingEventDao, accountDao: AccountDao){
        handleNotificationCountForNewEmail(notificationId)
        val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao, storage, accountDao)
        val chosenLabel = Label.LABEL_TRASH
        val currentLabel = Label.defaultItems.inbox
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel)))
        val peerRemoveLabels = if(currentLabel == Label.defaultItems.trash
                || currentLabel == Label.defaultItems.spam)
            listOf(currentLabel.text)
        else
            emptyList()

        val email = emailDao.getEmailByMetadataKey(metadataKey)

        peerEventsApiHandler.enqueueEvent(
                PeerChangeEmailLabelData(
                emailDao.getAllEmailsbyId(listOf(email.id)).map { it.metadataKey },
                peerRemoveLabels, selectedLabels.toList().map { it.text }).toJSON()
        )

        val result = Result.of {
            val emailIds = listOf(emailDao.getEmailByMetadataKey(metadataKey).id)

            val emailLabels = arrayListOf<EmailLabel>()
            emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    emailLabels.add(EmailLabel(
                            emailId = emailId,
                            labelId = labelId))
                }
            }
            db.createLabelEmailRelations(emailLabels)
            db.setTrashDate(emailIds)
        }

        return when (result) {
            is Result.Success -> {

            }
            is Result.Failure -> {
                showErrorNotification(UIMessage(R.string.push_email_error_title),
                        UIMessage(R.string.push_mail_error_message_trash))
            }
        }
    }

    fun showErrorNotification(title: UIMessage, body: UIMessage){
        val data = PushData.Error(title, body, isPostNougat, true)
        val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                null, data = data)
        notifyPushEvent(data = data, cn = not, notification = errorNot)
    }

    private fun handleNotificationCountForNewEmail(notificationId: Int){
        val notCount = storage.getInt(KeyValueStorage.StringKey.NewMailNotificationCount, 0)
        manager.cancel(notificationId)
        if((notCount - 1) == 0) {
            manager.cancel(CriptextNotification.INBOX_ID)
        }
        storage.putInt(KeyValueStorage.StringKey.NewMailNotificationCount, notCount - 1)
    }

    private fun postNotification(data: PushData.Error, cn: CriptextNotification,
                                 notification: Notification) {
        cn.notify(if(isPostNougat) PushTypes.linkDevice.requestCodeRandom() else
            PushTypes.linkDevice.requestCode(), notification,
                CriptextNotification.ACTION_LINK_DEVICE)
    }

    private fun postHeaderNotification(cn: CriptextNotification, data: PushData.Error){
        cn.showHeaderNotification(data.title.toString(), R.drawable.push_icon,
                CriptextNotification.ACTION_ERROR)
    }

    private fun notifyPushEvent(data: PushData.Error, cn: CriptextNotification,
                                notification: Notification) {
        if(isPostNougat) {
            postHeaderNotification(cn, data)
        }
        postNotification(data, cn, notification)
    }
}
