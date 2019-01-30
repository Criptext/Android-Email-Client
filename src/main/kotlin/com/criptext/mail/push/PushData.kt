package com.criptext.mail.push

import android.graphics.Bitmap
import com.criptext.mail.utils.DeviceUtils
import com.criptext.mail.utils.UIMessage

/**
 * POJOs used by PushController
 * Created by gabriel on 8/21/17.
 */

sealed class PushData {

    /**
     * POJO that holds all the data from the NewMail push notification
     */
    data class NewMail(val name: String, val email: String, val subject: String, val threadId: String,
                       val metadataKey: Long, val isPostNougat: Boolean, val preview: String,
                       val shouldPostNotification:Boolean, val activeEmail: String, val senderImage: Bitmap?): PushData()
    data class OpenMailbox(val title: String, val body: String,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean): PushData()

    data class Error(val title: UIMessage, val body: UIMessage,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean): PushData()

    data class LinkDevice(val title: String, val body: String, val randomId: String,
                          val deviceType: DeviceUtils.DeviceType, val deviceName: String, val syncFileVersion: Int,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean): PushData()

    data class SyncDevice(val title: String, val body: String, val randomId: String, val deviceId: Int,
                          val deviceType: DeviceUtils.DeviceType, val deviceName: String, val syncFileVersion: Int,
                          val isPostNougat: Boolean, val shouldPostNotification:Boolean): PushData()
}
