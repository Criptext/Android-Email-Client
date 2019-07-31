package com.criptext.mail.push.data

import android.app.Notification
import android.app.NotificationManager
import android.os.Build
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.api.*
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
import com.criptext.mail.scenes.label_chooser.SelectedLabels
import com.criptext.mail.scenes.label_chooser.data.LabelWrapper
import com.criptext.mail.utils.ServerCodes
import com.criptext.mail.utils.UIMessage
import com.criptext.mail.utils.peerdata.PeerChangeEmailLabelData
import com.criptext.mail.utils.peerdata.PeerOpenEmailData
import com.criptext.mail.utils.peerdata.PeerReadEmailData
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.flatMap
import org.json.JSONObject

class PushAPIRequestHandler(private val not: CriptextNotification,
                            private val manager: NotificationManager,
                            val activeAccount: ActiveAccount,
                            val httpClient: HttpClient,
                            private val storage: KeyValueStorage){

    private val apiClient = PushAPIClient(httpClient, activeAccount.jwt)

    private val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    fun linkAccept(deviceId: String, notificationId: Int): Int {
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
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
                val exception = operation.error
                val errorMessage = if(exception is ServerErrorException &&
                        exception.errorCode == ServerCodes.MethodNotAllowed)
                    UIMessage(R.string.sync_version_incorrect)
                else
                    UIMessage(R.string.push_link_error_message_approve)
                val data = PushData.Error(UIMessage(R.string.push_link_error_title),
                        errorMessage, isPostNougat, true)
                val errorNot = not.createNotification(CriptextNotification.ERROR_ID,
                        null, data)
                notifyPushEvent(data = data, cn = not, notification = errorNot)
                -1
            }
        }
    }

    fun linkDeny(deviceId: String, notificationId: Int){
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
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
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
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
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
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
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.NewMailNotificationCount, CriptextNotification.INBOX_ID)
        val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao, storage, accountDao)
        val operation = Result.of {
            val email = emailDao.getEmailByMetadataKey(metadataKey, activeAccount.id)
            email
        }
        .flatMap { Result.of { emailDao.toggleCheckingRead(listOf(it.id), false, activeAccount.id) } }



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
        handleNotificationCount(notificationId, KeyValueStorage.StringKey.NewMailNotificationCount, CriptextNotification.INBOX_ID)
        val peerEventsApiHandler = PeerEventsApiHandler.Default(httpClient, activeAccount, pendingDao, storage, accountDao)
        val chosenLabel = Label.LABEL_TRASH
        val currentLabel = Label.defaultItems.inbox
        val selectedLabels = SelectedLabels()
        selectedLabels.add(LabelWrapper(db.getLabelByName(chosenLabel, activeAccount.id)))
        val peerRemoveLabels = if(currentLabel == Label.defaultItems.trash
                || currentLabel == Label.defaultItems.spam)
            listOf(currentLabel.text)
        else
            emptyList()

        val email = emailDao.getEmailByMetadataKey(metadataKey, activeAccount.id)

        peerEventsApiHandler.enqueueEvent(
                PeerChangeEmailLabelData(
                emailDao.getAllEmailsbyId(listOf(email.id), activeAccount.id).map { it.metadataKey },
                peerRemoveLabels, selectedLabels.toList().map { it.text }).toJSON()
        )

        val result = Result.of {
            val emailIds = listOf(emailDao.getEmailByMetadataKey(metadataKey, activeAccount.id).id)

            val emailLabels = arrayListOf<EmailLabel>()
            emailIds.flatMap{ emailId ->
                selectedLabels.toIDs().map{ labelId ->
                    emailLabels.add(EmailLabel(
                            emailId = emailId,
                            labelId = labelId))
                }
            }
            db.createLabelEmailRelations(emailLabels)
            db.setTrashDate(emailIds, activeAccount.id)
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

    private fun handleNotificationCount(notificationId: Int, key: KeyValueStorage.StringKey, id: Int){
        val notCount = storage.getInt(key, 0)
        manager.cancel(notificationId)
        if((notCount - 1) == 0) {
            manager.cancel(id)
        }
        storage.putInt(key, notCount - 1)
    }

    private fun postNotification(cn: CriptextNotification,
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
        postNotification(cn, notification)
    }
}
