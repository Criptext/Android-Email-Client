package com.criptext.mail.scenes.linking

class LinkingModel(val email: String) {
    var untrustedDevicePostedKeyBundle: Boolean = false
    var remoteDeviceId: Int = 0
}