package com.criptext.mail.scenes.settings.privacy

import com.criptext.mail.scenes.SceneModel

class PrivacyModel: SceneModel {
    var readReceipts: Boolean = false
    var twoFA: Boolean = false
    var isEmailConfirmed: Boolean = false
    var blockRemoteContent: Boolean = true
}