package com.criptext.mail.scenes.linking

import com.criptext.mail.db.models.ActiveAccount
import com.criptext.mail.signal.PreKeyBundleShareData
import com.criptext.mail.utils.DeviceUtils

class LinkingModel(val incomingAccount: ActiveAccount, var remoteDeviceId: Int, val randomId: String,
                   val deviceType: DeviceUtils.DeviceType) {
    var untrustedDevicePostedKeyBundle: Boolean = false
    var dataFileHasBeenCreated = false
    var dataFilePath = ""
    var dataFileKey: ByteArray? = null
    var keyBundle: PreKeyBundleShareData.DownloadBundle? = null
    var retryTimesCheckForKeyBundle = 0
}