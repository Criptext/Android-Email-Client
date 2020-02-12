package com.criptext.mail.push

import android.graphics.Bitmap
import android.os.IBinder
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage

/**
 * POJOs used by PushController
 * Created by gabriel on 8/21/17.
 */

sealed class PushData {

    data class NewMail(val name: String, val email: String, val subject: String, val threadId: String,
                       val metadataKey: Long, val isPostNougat: Boolean, val preview: String, val hasInlineImages: Boolean,
                       val shouldPostNotification:Boolean, val activeEmail: String, val senderImage: Bitmap?,
                       val recipientId: String, val account: String, val domain: String): PushData() {
        companion object {
            fun parseNewMailPush(pushData: Map<String, String>, isPostNougat: Boolean,
                                 shouldPostNotification: Boolean, senderImage: Bitmap?): NewMail {
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

                return NewMail(name = name, email = email, subject = subject, threadId = threadId,
                        metadataKey = metadataKey ?: -1, shouldPostNotification = shouldPostNotification,
                        isPostNougat = isPostNougat, preview = preview, activeEmail = "$account@$domain",
                        senderImage = senderImage, hasInlineImages = hasInlineImages, recipientId = recipientId,
                        account = account, domain = domain)
            }
        }
    }
    data class OpenMailbox(val title: String, val body: String, val recipientId: String, val subject: String,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean, val domain: String): PushData(){
        companion object{
            fun parseNewOpenMailbox(pushData: Map<String, String>, isPostNougat: Boolean,
                                    shouldPostNotification: Boolean): OpenMailbox {
                val body = pushData["body"] ?: ""
                val title = pushData["title"] ?: ""
                val recipientId = pushData["recipientId"] ?: ""
                val domain = pushData["domain"] ?: ""
                val subject = pushData["subject"] ?: ""

                return OpenMailbox(title = title, body = body, subject = subject,
                        shouldPostNotification = shouldPostNotification,
                        isPostNougat = isPostNougat, recipientId = recipientId, domain = domain)
            }
        }
    }

    data class Error(val title: UIMessage, val body: UIMessage,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean): PushData()

    data class LinkDevice(val title: String, val body: String, val randomId: String, val recipientId: String,
                          val deviceType: DeviceUtils.DeviceType, val deviceName: String, val syncFileVersion: Int,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean, val domain: String): PushData(){
        companion object{
            fun parseLinkDevicePush(pushData: Map<String, String>, isPostNougat: Boolean,
                                    shouldPostNotification: Boolean): LinkDevice {
                val body = pushData["body"] ?: ""
                val title = pushData["title"] ?: ""
                val deviceId = pushData["randomId"] ?: ""
                val deviceType = pushData["deviceType"] ?: ""
                val deviceName = pushData["deviceName"] ?: ""
                val syncFileVersion = pushData["version"] ?: ""
                val recipientId = pushData["recipientId"] ?: ""
                val domain = pushData["domain"] ?: ""

                return LinkDevice(title = title, body = body, deviceName = deviceName,
                        shouldPostNotification = shouldPostNotification, recipientId = recipientId,
                        isPostNougat = isPostNougat, randomId = deviceId, syncFileVersion = syncFileVersion.toInt(),
                        deviceType = DeviceUtils.getDeviceType(deviceType.toInt()), domain = domain)
            }
        }
    }

    data class SyncDevice(val title: String, val body: String, val randomId: String, val deviceId: Int,
                          val deviceType: DeviceUtils.DeviceType, val deviceName: String, val syncFileVersion: Int,
                          val isPostNougat: Boolean, val shouldPostNotification:Boolean, val recipientId: String,
                          val domain: String): PushData(){
        companion object{
            fun parseSyncDevicePush(pushData: Map<String, String>, isPostNougat: Boolean,
                                    shouldPostNotification: Boolean): SyncDevice {
                val body = pushData["body"] ?: ""
                val title = pushData["title"] ?: ""
                val randomId = pushData["randomId"] ?: ""
                val deviceId = pushData["deviceId"] ?: ""
                val deviceType = pushData["deviceType"] ?: ""
                val deviceName = pushData["deviceName"] ?: ""
                val syncFileVersion = pushData["version"] ?: ""
                val recipientId = pushData["recipientId"] ?: ""
                val domain = pushData["domain"] ?: ""

                return SyncDevice(title = title, body = body, deviceName = deviceName,
                        shouldPostNotification = shouldPostNotification,
                        isPostNougat = isPostNougat, randomId = randomId, deviceId = deviceId.toInt(),
                        syncFileVersion = syncFileVersion.toInt(), recipientId = recipientId,
                        deviceType = DeviceUtils.getDeviceType(deviceType.toInt()), domain = domain)
            }
        }
    }

    data class JobBackup(val title: String, val body: String, val isPostNougat: Boolean,
                         val shouldPostNotification:Boolean, val recipientId: String,
                         val domain: String, val progress: Int): PushData()
}
