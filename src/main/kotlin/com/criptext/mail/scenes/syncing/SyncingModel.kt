package com.criptext.mail.scenes.syncing

import com.criptext.mail.scenes.syncing.holders.SyncingLayoutState
import com.criptext.mail.utils.DeviceUtils

class SyncingModel() {
    var state: SyncingLayoutState = SyncingLayoutState.SyncBegin()
    var email: String = ""
    var remoteDeviceId: Int = -1
    var randomId: String = ""
    var deviceType: DeviceUtils.DeviceType = DeviceUtils.DeviceType.Android
    var authorizerName: String = ""
    var name = ""
    var key = ""
    var dataAddress = ""
    var retryTimeLinkDataReady = 0
    var retryTimeLinkStatus = 0
}