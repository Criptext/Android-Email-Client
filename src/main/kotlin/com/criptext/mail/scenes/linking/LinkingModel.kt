package com.criptext.mail.scenes.linking

import com.criptext.mail.signal.PreKeyBundleShareData

class LinkingModel(val email: String, var remoteDeviceId: Int, val randomId: String) {
    var untrustedDevicePostedKeyBundle: Boolean = false
    var dataFileHasBeenCreated = false
    var dataFilePath = ""
    var dataFileKey: ByteArray? = null
    var keyBundle: PreKeyBundleShareData.DownloadBundle? = null
    var retryTimesCheckForKeyBundle = 0
}