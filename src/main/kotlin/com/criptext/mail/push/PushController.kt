package com.criptext.mail.push

import com.criptext.mail.R
import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.db.models.Label
import com.criptext.mail.push.data.PushDataSource
import com.criptext.mail.push.data.PushRequest
import com.criptext.mail.push.data.PushResult
import com.criptext.mail.services.MessagingService
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage

/**
 * Controller designed to be used by EmailFirebaseMessageService. Exposes a single function:
 * parsePushPayload() which takes in the Map object with the push data and returns a Notifier
 * object, which the service can use to display a notification by invoking its notifyPushEvent()
 * method.
 *
 * The constructor receives a lambda function that performs a partial update. This function is
 * expected to throw a few exceptions related to connectivity issues every now and then, which are
 * handled by the controller to configure the resulting Notifier object.
 * Created by gabriel on 8/18/17.
 */

class PushController(private val dataSource: PushDataSource, private val host: MessagingService,
                     private val isPostNougat: Boolean, private val activeAccount: ActiveAccount) {

    private val dataSourceListener = { result: PushResult ->
        when (result) {
            is PushResult.UpdateMailbox -> onUpdateMailbox(result)
        }
    }

    init {
        dataSource.listener = dataSourceListener
    }


    private fun parseNewMailPush(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean): PushData.NewMail {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""
        val threadId = pushData["threadId"] ?: ""
        val metadataKey = pushData["metadataKey"]?.toLong()
        val preview = pushData["preview"] ?: ""

        return PushData.NewMail(title = title, body = body, threadId = threadId,
                metadataKey = metadataKey ?: -1, shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat, preview = preview, activeEmail = activeAccount.userEmail)
    }

    private fun parseNewOpenMailbox(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean): PushData.OpenMailbox {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""

        return PushData.OpenMailbox(title = title, body = body,
                shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat)
    }

    private fun parseLinkDevicePush(pushData: Map<String, String>,
                                 shouldPostNotification: Boolean): PushData.LinkDevice {
        val body = pushData["body"] ?: ""
        val title = pushData["title"] ?: ""
        val deviceId = pushData["randomId"] ?: ""
        val deviceType = pushData["deviceType"] ?: ""
        val deviceName = pushData["deviceName"] ?: ""

        return PushData.LinkDevice(title = title, body = body, deviceName = deviceName,
                shouldPostNotification = shouldPostNotification,
                isPostNougat = isPostNougat, randomId = deviceId,
                deviceType = DeviceUtils.getDeviceType(deviceType.toInt()))
    }

    fun parsePushPayload(pushData: Map<String, String>, shouldPostNotification: Boolean) {
        dataSource.submitRequest(PushRequest.UpdateMailbox(Label.defaultItems.inbox, null,
                pushData, shouldPostNotification))
    }

    private fun createAndNotifyPush(pushData: Map<String, String>, shouldPostNotification: Boolean,
                                    isSuccess: Boolean){
        val action = pushData["action"]
        if (action != null) {
            val type = PushTypes.fromActionString(action)
            val notifier =  when (type) {
                PushTypes.newMail -> {
                    if(isSuccess) {
                        val data = parseNewMailPush(pushData, shouldPostNotification)
                        NewMailNotifier.Single(data)
                    }else{
                        val data = PushData.Error(UIMessage(R.string.push_email_update_mailbox_title),
                                UIMessage(R.string.push_email_update_mailbox_body), isPostNougat, shouldPostNotification)
                        ErrorNotifier.Open(data)
                    }
                }
                PushTypes.linkDevice -> {
                    val data = parseLinkDevicePush(pushData, shouldPostNotification)
                    LinkDeviceNotifier.Open(data)
                }
                PushTypes.openActivity -> {
                    val data = parseNewOpenMailbox(pushData, shouldPostNotification)
                    OpenMailboxNotifier.Open(data)
                }

            }
            host.notifyPushEvent(notifier)
        }
    }

    private fun onUpdateMailbox(result: PushResult.UpdateMailbox){
        when(result){
            is PushResult.UpdateMailbox.Success -> {
                createAndNotifyPush(result.pushData, result.shouldPostNotification, true)
            }
            is PushResult.UpdateMailbox.Failure -> {
                createAndNotifyPush(result.pushData, result.shouldPostNotification, false)
            }
        }
    }


}