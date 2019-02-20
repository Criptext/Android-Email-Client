package com.criptext.mail.scenes.settings.privacyandsecurity

import com.criptext.mail.scenes.SceneModel

class PrivacyAndSecurityModel(val hasReadReceipts: Boolean): SceneModel{
    var pinTimeOut = 1
    var pinActive = false
}