package com.criptext.mail.push

import android.graphics.Bitmap
import com.criptext.mail.R
import com.criptext.mail.androidui.CriptextNotification
import com.criptext.mail.db.KeyValueStorage
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.data.PushRequest
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.push.notifiers.*
import com.criptext.mail.services.DecryptionForegroundService
import com.criptext.mail.services.DecryptionService
import com.criptext.mail.services.MessagingService
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.eventhelper.EventHelper
import com.criptext.mail.utils.UIMessage

class PushController(private val dataSource: PushDataSource, private val host: DecryptionService,
                     private val isPostNougat: Boolean, private val activeAccount: ActiveAccount,
                     private val storage: KeyValueStorage) {

    private val dataSourceListener = { result: PushResult ->
        when (result) {
            is PushResult.UpdateMailbox -> onUpdateMailbox(result)
            is PushResult.NewEmail -> onNewEmail(result)
            is PushResult.RemoveNotification -> onRemoveNotification(result)
        }
    }

    init {
        dataSource.listener = dataSourceListener
    }


    private fun parseNewMailPush(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean, senderImage: Bitmap?): PushData.NewMail {
        val subject = pushData["subject"] ?: ""
        val name = pushData["name"] ?: ""
        val email = pushData["email"] ?: ""
        val threadId = pushData["threadId"] ?: ""
        val metadataKey = pushData["metadataKey"]?.toLong()
        val preview = pushData["preview"] ?: ""
        val hasInlineImages = pushData["hasInlineImages"]?.toBoolean() ?: false
        val recipientId = pushData["recipientId"] ?: ""
        val account = pushData["account"] ?: ""
        val domain = pushData["domain"] ?: ""

        return PushData.NewMail(name = name, email = email, subject = subject, threadId = threadId,
                metadataKey = metadataKey ?: -1, shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat, preview = preview, activeEmail = activeAccount.userEmail,
                senderImage = senderImage, hasInlineImages = hasInlineImages, recipientId = recipientId,
                account = account, domain = domain)
    }

    private fun parseNewOpenMailbox(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean): PushData.OpenMailbox {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""
        val recipientId = pushData["recipientId"] ?: ""
        val domain = pushData["domain"] ?: ""
        val subject = pushData["subject"] ?: ""

        return PushData.OpenMailbox(title = title, body = body, subject = subject,
                shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat, recipientId = recipientId, domain = domain)
    }

    private fun parseLinkDevicePush(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean): PushData.LinkDevice {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""
        val deviceId = pushData["randomId"] ?: ""
        val deviceType = pushData["deviceType"] ?: ""
        val deviceName = pushData["deviceName"] ?: ""
        val syncFileVersion = pushData["version"] ?: ""
        val recipientId = pushData["recipientId"] ?: ""
        val domain = pushData["domain"] ?: ""

        return PushData.LinkDevice(title = title, body = body, deviceName = deviceName,
                shouldPostNotification = shouldPostNotification, recipientId = recipientId,
                isPostNougat = isPostNougat, randomId = deviceId, syncFileVersion = syncFileVersion.toInt(),
                deviceType = DeviceUtils.getDeviceType(deviceType.toInt()), domain = domain)
    }

    private fun parseSyncDevicePush(pushData: Map<String, String>,
                                    shouldPostNotification: Boolean): PushData.SyncDevice {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""
        val randomId = pushData["randomId"] ?: ""
        val deviceId = pushData["deviceId"] ?: ""
        val deviceType = pushData["deviceType"] ?: ""
        val deviceName = pushData["deviceName"] ?: ""
        val syncFileVersion = pushData["version"] ?: ""
        val recipientId = pushData["recipientId"] ?: ""
        val domain = pushData["domain"] ?: ""

        return PushData.SyncDevice(title = title, body = body, deviceName = deviceName,
                shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat, randomId = randomId, deviceId = deviceId.toInt(),
                syncFileVersion = syncFileVersion.toInt(), recipientId = recipientId,
                deviceType = DeviceUtils.getDeviceType(deviceType.toInt()), domain = domain)
    }

    fun parsePushPayload(pushData: Map<String, String>, shouldPostNotification: Boolean) {
        if(shouldPostNotification)
            dataSource.submitRequest(PushRequest.NewEmail(Label.defaultItems.inbox,
                    pushData, shouldPostNotification))
    }

    fun doGetEvents(){
        dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox))
    }

    private fun createAndNotifyPush(pushData: Map<String, String>, shouldPostNotification: Boolean,
                                    isSuccess: Boolean, senderImage: Bitmap?, notificationId: Int){
        val action = pushData["action"]
        if (action != null) {
            val type = PushTypes.fromActionString(action)
            val notifier =  when (type) {
                PushTypes.newMail -> {
                    if(isSuccess) {
                        val data = parseNewMailPush(pushData, shouldPostNotification, senderImage)
                        NewMailNotifier.Single(data, notificationId)
                    }else{
                        val data = PushData.Error(UIMessage(R.string.push_email_update_mailbox_title),
                                UIMessage(R.string.push_email_update_mailbox_body), isPostNougat, shouldPostNotification)
                        ErrorNotifier.Open(data)
                    }
                }
                PushTypes.linkDevice -> {
                    val data = parseLinkDevicePush(pushData, shouldPostNotification)
                    LinkDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.openActivity -> {
                    val data = parseNewOpenMailbox(pushData, shouldPostNotification)
                    OpenMailboxNotifier.Open(data)
                }
                PushTypes.syncDevice -> {
                    val data = parseSyncDevicePush(pushData, shouldPostNotification)
                    SyncDeviceNotifier.Open(data, notificationId)
                }
                PushTypes.antiPush -> {
                    val subAction = pushData["subAction"]
                    when(subAction){
                        "delete_new_email" -> {
                            val metadataKeys = pushData["metadataKeys"]?.split(",")
                            metadataKeys?.forEach {
                                dataSource.submitRequest(PushRequest.RemoveNotification(pushData, it))
                            }
                        }
                        "delete_sync_link" -> {
                            val randomId = pushData["randomId"] ?: ""
                            if(randomId.isNotEmpty()) {
                                dataSource.submitRequest(PushRequest.RemoveNotification(pushData, randomId))
                            }
                        }
                    }
                    null
                }
                else -> null
            }
            host.notifyPushEvent(notifier)
        }
    }

    private fun onRemoveNotification(result: PushResult.RemoveNotification){
        when(result){
            is PushResult.RemoveNotification.Success -> {
                when(result.antiPushSubtype) {
                    "delete_new_email" -> {
                        host.cancelPush(result.notificationId, storage,
                                KeyValueStorage.StringKey.NewMailNotificationCount, CriptextNotification.INBOX_ID)
                    }
                    "delete_sync_link" -> {
                        host.cancelPush(result.notificationId, storage,
                                KeyValueStorage.StringKey.SyncNotificationCount, CriptextNotification.LINK_DEVICE_ID)
                    }
                }
            }
        }
    }

    private fun onUpdateMailbox(result: PushResult.UpdateMailbox){
        when(result){
            is PushResult.UpdateMailbox.SuccessAndRepeat -> {
                dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox))
            }
            else -> host.endService()
        }
    }

    private fun onNewEmail(result: PushResult.NewEmail){
        when(result){
            is PushResult.NewEmail.Success -> {
                createAndNotifyPush(result.pushData, result.shouldPostNotification, true,
                        result.senderImage, result.notificationId)
            }
            is PushResult.NewEmail.Failure -> {
                if(result.exception !is EventHelper.NoContentFoundException)
                    createAndNotifyPush(result.pushData, result.shouldPostNotification, false,
                            null, result.notificationId)
            }
        }
        host.pushProcessed()
    }


}