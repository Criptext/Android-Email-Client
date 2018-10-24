package com.criptext.mail.push

import com.criptext.mail.utils.DeviceUtils

/**
 * POJOs used by PushController
 * Created by gabriel on 8/21/17.
 */

class PushData {

    /**
     * POJO that holds all the data from the NewMail push notification
     */
    data class NewMail(val title: String, val body: String, val threadId: String,
                       val metadataKey: Long, val isPostNougat: Boolean,
                       val shouldPostNotification:Boolean)
    data class OpenMailbox(val title: String, val body: String,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean)

    data class LinkDevice(val title: String, val body: String, val randomId: String,
                          val deviceType: DeviceUtils.DeviceType,
                           val isPostNougat: Boolean, val shouldPostNotification:Boolean)
}
