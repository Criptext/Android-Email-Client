package com.criptext.mail.scenes.linking

class LinkingModel(val email: String) {
    var untrustedDevicePostedKeyBundle: Boolean = false
    var remoteDeviceId: Int = 0
    var dataFileHasBeenCreated = false
    var dataFilePath = ""
    var dataFileKey: ByteArray? = null
}