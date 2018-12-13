package com.criptext.mail.scenes.settings.syncing

import com.criptext.mail.utils.DeviceUtils

class SyncingModel(val email: String, val remoteDeviceId: Int, val randomId: String,
                   val deviceType: DeviceUtils.DeviceType, val authorizerName: String) {
    var name = ""
    var key = ""
    var dataAddress = ""
    var retryTimeLinkDataReady = 0
    var retryTimeLinkStatus = 0
}